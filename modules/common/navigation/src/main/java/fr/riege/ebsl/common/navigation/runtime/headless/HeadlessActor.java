package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationActor;

public final class HeadlessActor implements NavigationActor {
    private Vec3d position;
    private Vec3d velocity = new Vec3d(0.0, 0.0, 0.0);
    private boolean onGround = true;
    private boolean alive = true;
    private double width = 0.6;
    private double height = 1.8;

    public HeadlessActor(Vec3d position) {
        this.position = position;
    }

    @Override public Vec3d position() {
        return position;
    }

    @Override public Vec3d velocity() {
        return velocity;
    }

    @Override public boolean onGround() {
        return onGround;
    }

    @Override public boolean isAlive() {
        return alive;
    }

    @Override public double width() {
        return width;
    }

    @Override public double height() {
        return height;
    }

    public HeadlessActor position(Vec3d position) {
        this.position = position;
        return this;
    }

    public HeadlessActor velocity(Vec3d velocity) {
        this.velocity = velocity;
        return this;
    }

    public HeadlessActor onGround(boolean onGround) {
        this.onGround = onGround;
        return this;
    }

    public HeadlessActor alive(boolean alive) {
        this.alive = alive;
        return this;
    }

    public HeadlessActor size(double width, double height) {
        this.width = Math.max(0.0, width);
        this.height = Math.max(0.0, height);
        return this;
    }

    public void tick(HeadlessWorldLayer world) {
        Vec3d next = position.add(velocity.x(), velocity.y(), velocity.z());
        int footX = (int) Math.floor(next.x());
        int footY = (int) Math.floor(next.y());
        int footZ = (int) Math.floor(next.z());
        if (world != null && world.isSolid(footX, footY, footZ)) {
            velocity = new Vec3d(0.0, 0.0, 0.0);
            onGround = true;
            return;
        }
        position = next;
        if (world != null && world.isSolid(footX, footY - 1, footZ) && velocity.y() <= 0.0) {
            onGround = true;
            velocity = new Vec3d(velocity.x(), 0.0, velocity.z());
        } else {
            onGround = false;
            velocity = new Vec3d(velocity.x(), velocity.y() - 0.08, velocity.z());
        }
    }
}
