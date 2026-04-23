package fr.riege.ebsl.pathfinding.rotation.strategy;

import fr.riege.ebsl.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.pathfinding.rotation.IRotationStrategy;
import fr.riege.ebsl.pathfinding.rotation.Rotation;
import net.minecraft.client.player.LocalPlayer;

import java.util.Random;

/**
 * Human-like rotation strategy.
 *
 * Each tick, moves a fraction of the remaining angle toward the target.
 * This produces natural exponential deceleration: fast initial snap that
 * smoothly eases into the target, rather than a constant-speed or timed approach.
 *
 * The lerp fraction scales with the remaining delta:
 *   large angle  -> high fraction (fast approach)
 *   small angle  -> low fraction  (slow, drifting correction)
 *
 * A small random yaw overshoot is applied when first beginning a significant
 * rotation, then corrected naturally via the same lerp. Overshoot magnitude
 * scales with (1 - difficulty): tight corridors get precise movement, open
 * areas get a subtle human wobble.
 *
 * @param difficulty  0 = open/easy (max overshoot), 1 = tight/hard (no overshoot).
 */
public final class HumanRotationStrategy implements IRotationStrategy {

    // Fraction of remaining angle to cover each tick.
    // Lerped based on |remaining delta|: fast for large turns, slow for tiny corrections.
    private static final float MAX_LERP    = 0.30f;  // 30 % per tick for large angles
    private static final float MIN_LERP    = 0.08f;  // 8 %  per tick for tiny corrections
    private static final float FAST_THRESH = 25.0f;  // degrees above which MAX_LERP applies

    // Pitch settles noticeably slower than yaw - feels more natural
    private static final float PITCH_LERP_SCALE = 0.55f;

    // Overshoot: degrees past the real target, at difficulty = 0
    private static final float MAX_OVERSHOOT_DEG  = 4.5f;
    private static final float OVERSHOOT_MIN_DELTA = 8.0f;  // only overshoot for non-trivial turns

    // Strategy is considered done when within this many degrees of the real target
    private static final float DONE_THRESH = 1.0f;

    private static final Random RANDOM = new Random();

    private final float difficulty;

    private boolean initialized;
    private float   overshootYaw;
    private boolean inOvershoot;

    public HumanRotationStrategy(float difficulty) {
        this.difficulty = Math.max(0f, Math.min(1f, difficulty));
    }

    @Override
    public void onStart() {
        initialized = false;
        inOvershoot = false;
        overshootYaw = 0f;
    }

    @Override
    public Rotation onRotate(LocalPlayer player, float targetYaw, float targetPitch) {
        float curYaw   = player.getYRot();
        float curPitch = player.getXRot();

        // First call: decide whether to apply an overshoot
        if (!initialized) {
            float yawDelta = AngleUtils.normalizeAngle(targetYaw - curYaw);
            float openness = 1.0f - difficulty;
            float overshootAmt = MAX_OVERSHOOT_DEG * openness * (0.4f + 0.6f * RANDOM.nextFloat());
            if (Math.abs(yawDelta) > OVERSHOOT_MIN_DELTA && overshootAmt > 0.5f) {
                overshootYaw = targetYaw + Math.signum(yawDelta) * overshootAmt;
                inOvershoot  = true;
            }
            initialized = true;
        }

        float effYaw   = inOvershoot ? overshootYaw : targetYaw;
        float yawDelta   = AngleUtils.normalizeAngle(effYaw - curYaw);
        float pitchDelta = AngleUtils.normalizeAngle(targetPitch - curPitch);

        // Lerp a fraction of the remaining angle - produces natural easing
        float yawStep   = yawDelta   * lerpFactor(Math.abs(yawDelta));
        float pitchStep = pitchDelta * lerpFactor(Math.abs(pitchDelta)) * PITCH_LERP_SCALE;

        float newYaw   = curYaw   + yawStep;
        float newPitch = Math.max(-90f, Math.min(90f, curPitch + pitchStep));

        // Exit overshoot once we've reached the overshoot position
        if (inOvershoot && Math.abs(yawDelta) < DONE_THRESH) {
            inOvershoot = false;
        }

        // Done when close enough to the REAL target and not still overshooting
        float realRemaining = Math.abs(AngleUtils.normalizeAngle(targetYaw - newYaw));
        if (!inOvershoot
                && realRemaining              < DONE_THRESH
                && Math.abs(targetPitch - newPitch) < DONE_THRESH) {
            return null; // signal completion - player stays at current angle, no snap
        }

        return new Rotation(newYaw, newPitch);
    }

    /**
     * Lerp fraction for the given remaining angle.
     * Scales linearly from MIN_LERP (tiny corrections) to MAX_LERP (large turns).
     */
    private static float lerpFactor(float absDelta) {
        float t = Math.min(1f, absDelta / FAST_THRESH);
        return MIN_LERP + (MAX_LERP - MIN_LERP) * t;
    }
}
