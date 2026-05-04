package fr.riege.ebsl.pathfinding.rotation;

import net.minecraft.client.Minecraft;

/**
 * Handles smooth player rotations with GCD (game cursor distance) simulation.
 * Call {@link #update(Minecraft)} every client tick.
 */
public final class RotationExecutor {

    private static final Minecraft mc = Minecraft.getInstance();

    private static float             targetYaw;
    private static float             targetPitch;
    private static IRotationStrategy currStrat;
    private static boolean           isRotating = false;
    /** GCD cached per navigation start - recomputed in rotateTo() when strategy changes. */
    private static double            cachedGcd  = Double.NaN;

    private RotationExecutor() {}

    public static void rotateTo(Rotation endRot, IRotationStrategy strategy) {
        RotationDebug.log("rotation", "rotateTo request current=(yaw=%.2f,pitch=%.2f) target=(yaw=%.2f,pitch=%.2f) strat=%s",
                mc.player != null ? mc.player.getYRot() : 0.0f,
                mc.player != null ? mc.player.getXRot() : 0.0f,
                endRot.yaw,
                endRot.pitch,
                strategy != null ? strategy.getClass().getSimpleName() : "null");
        stopRotating();
        targetYaw   = endRot.yaw;
        targetPitch = endRot.pitch;
        currStrat   = strategy;
        // Cache GCD once per navigation start instead of recomputing every tick
        double sens = mc.options.sensitivity().get();
        double f    = sens * 0.6 + 0.2;
        cachedGcd   = f * f * f * 1.2;
        RotationDebug.log("rotation", "rotateTo cached gcd=%.5f sensitivity=%.3f", cachedGcd, sens);
        strategy.onStart();
        isRotating  = true;
    }

    public static void stopRotating() {
        if (currStrat != null || isRotating) {
            RotationDebug.log("rotation", "stopRotating target=(yaw=%.2f,pitch=%.2f) strat=%s",
                    targetYaw,
                    targetPitch,
                    currStrat != null ? currStrat.getClass().getSimpleName() : "null");
        }
        if (currStrat != null) currStrat.onStop();
        currStrat  = null;
        isRotating = false;
    }

    public static boolean isRotating()  { return isRotating; }
    public static float   getTargetYaw() { return targetYaw; }
    public static float   getTargetPitch() { return targetPitch; }

    /** Called every client tick from AetherClient. */
    public static void update(Minecraft mc) {
        var player = mc.player;
        if (player == null || !isRotating) return;

        if (currStrat != null) {
            RotationDebug.log("rotation", "update start current=(yaw=%.2f,pitch=%.2f) target=(yaw=%.2f,pitch=%.2f) strat=%s",
                    player.getYRot(),
                    player.getXRot(),
                    targetYaw,
                    targetPitch,
                    currStrat.getClass().getSimpleName());
            Rotation result = currStrat.onRotate(player, targetYaw, targetPitch);
            if (result == null) {
                RotationDebug.log("rotation", "update strategy finished current=(yaw=%.2f,pitch=%.2f)",
                        player.getYRot(), player.getXRot());
                stopRotating();
            } else {
                float prevYaw = player.getYRot();
                float prevPitch = player.getXRot();
                float newYaw   = applyGCD(result.yaw, player.getYRot());
                float newPitch = Math.max(-90f, Math.min(90f,
                        applyGCD(result.pitch, player.getXRot(), -90f, 90f)));
                RotationDebug.log("rotation", "update apply raw=(yaw=%.2f,pitch=%.2f) prev=(yaw=%.2f,pitch=%.2f) final=(yaw=%.2f,pitch=%.2f)",
                        result.yaw, result.pitch, prevYaw, prevPitch, newYaw, newPitch);
                player.setYRot(newYaw);
                player.setXRot(newPitch);
                player.yRotO = prevYaw;
                player.xRotO = prevPitch;
                player.yHeadRotO = prevYaw;
                player.yBodyRotO = prevYaw;
                player.yHeadRot = newYaw;
                player.yBodyRot = newYaw;
            }
        }
    }

    /** Simulates Minecraft's GCD (game cursor distance) rounding. */
    private static float applyGCD(float rotation, float prevRotation) {
        return applyGCD(rotation, prevRotation, null, null);
    }

    private static float applyGCD(float rotation, float prevRotation, Float min, Float max) {
        // Use cached GCD (set once per rotateTo call) instead of recomputing every tick
        double gcd = Double.isNaN(cachedGcd) ? computeGcd() : cachedGcd;

        double delta        = AngleUtils.getRotationDelta(prevRotation, rotation);
        double roundedDelta = Math.round(delta / gcd) * gcd;
        float  result       = (float)(prevRotation + roundedDelta);

        if (max != null && result > max) result -= (float) gcd;
        if (min != null && result < min) result += (float) gcd;

        RotationDebug.log("rotation", "applyGCD prev=%.2f target=%.2f delta=%.2f rounded=%.5f gcd=%.5f result=%.2f clamp=[%s,%s]",
                prevRotation,
                rotation,
                delta,
                roundedDelta,
                gcd,
                result,
                min != null ? String.format("%.2f", min) : "null",
                max != null ? String.format("%.2f", max) : "null");

        return result;
    }

    private static double computeGcd() {
        double f = mc.options.sensitivity().get() * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}
