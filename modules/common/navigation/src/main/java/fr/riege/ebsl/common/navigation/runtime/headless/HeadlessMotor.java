package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.runtime.entity.MovementIntent;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationMotor;

public final class HeadlessMotor implements NavigationMotor {
    private final HeadlessActor actor;
    private MovementIntent lastIntent = MovementIntent.stop();

    public HeadlessMotor(HeadlessActor actor) {
        this.actor = actor;
    }

    public MovementIntent lastIntent() {
        return lastIntent;
    }

    @Override public void apply(MovementIntent intent) {
        lastIntent = intent == null ? MovementIntent.stop() : intent;
        Vec3d velocity = lastIntent.velocity();
        if (lastIntent.jump() && actor.onGround()) {
            velocity = new Vec3d(velocity.x(), Math.max(velocity.y(), 0.42), velocity.z());
        }
        actor.velocity(velocity);
    }
}
