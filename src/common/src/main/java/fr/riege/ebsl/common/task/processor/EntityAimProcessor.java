package fr.riege.ebsl.common.task.processor;

import fr.riege.ebsl.common.entity.EntitySnapshot;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.platform.EbslPlatform;

public final class EntityAimProcessor {
    private final AimProcessor aimProcessor = new AimProcessor();

    public boolean aimAt(EbslPlatform platform, EntitySnapshot target) {
        if (target == null || !platform.player().isAlive()) {
            return false;
        }
        Vec3d aimPoint = visibleAimPoint(platform, target);
        if (aimPoint == null) {
            aimProcessor.reset();
            return false;
        }
        aimProcessor.aimAt(platform, aimPoint);
        return true;
    }

    private static Vec3d visibleAimPoint(EbslPlatform platform, EntitySnapshot target) {
        Vec3d eye = target.eyePosition();
        if (platform.world().hasLineOfSight(platform.player().eyePosition(), eye)) {
            return eye;
        }

        double x = (target.minX() + target.maxX()) * 0.5;
        double z = (target.minZ() + target.maxZ()) * 0.5;
        Vec3d upperBody = new Vec3d(x, target.minY() + target.bbHeight() * 0.75, z);
        if (platform.world().hasLineOfSight(platform.player().eyePosition(), upperBody)) {
            return upperBody;
        }

        Vec3d centerMass = new Vec3d(x, target.minY() + target.bbHeight() * 0.5, z);
        if (platform.world().hasLineOfSight(platform.player().eyePosition(), centerMass)) {
            return centerMass;
        }

        Vec3d lowerBody = new Vec3d(x, target.minY() + target.bbHeight() * 0.35, z);
        return platform.world().hasLineOfSight(platform.player().eyePosition(), lowerBody) ? lowerBody : null;
    }

    public void reset() {
        aimProcessor.reset();
    }
}
