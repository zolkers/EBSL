package fr.riege.ebsl.general.task.processor;

import fr.riege.ebsl.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EntityAimProcessor {
    private final AimProcessor aimProcessor;

    public EntityAimProcessor() {
        this(new AimProcessor());
    }

    public EntityAimProcessor(AimProcessor aimProcessor) {
        this.aimProcessor = aimProcessor;
    }

    public boolean aimAt(Minecraft mc, Entity target) {
        if (mc.player == null || target == null) {
            return false;
        }
        Vec3 aimPoint = visibleAimPoint(mc, target);
        if (aimPoint == null) {
            aimProcessor.reset();
            return false;
        }
        aimProcessor.aimAt(mc, aimPoint);
        return true;
    }

    private static Vec3 visibleAimPoint(Minecraft mc, Entity target) {
        Vec3 eye = target.getEyePosition();
        if (ClientUtils.hasLineOfSight(mc.player, eye)) {
            return eye;
        }

        AABB box = target.getBoundingBox();
        double x = (box.minX + box.maxX) * 0.5;
        double z = (box.minZ + box.maxZ) * 0.5;
        Vec3 upperBody = new Vec3(x, box.minY + target.getBbHeight() * 0.75, z);
        if (ClientUtils.hasLineOfSight(mc.player, upperBody)) {
            return upperBody;
        }

        Vec3 centerMass = new Vec3(x, box.minY + target.getBbHeight() * 0.5, z);
        if (ClientUtils.hasLineOfSight(mc.player, centerMass)) {
            return centerMass;
        }

        Vec3 lowerBody = new Vec3(x, box.minY + target.getBbHeight() * 0.35, z);
        if (ClientUtils.hasLineOfSight(mc.player, lowerBody)) {
            return lowerBody;
        }

        return null;
    }

    public void reset() {
        aimProcessor.reset();
    }
}
