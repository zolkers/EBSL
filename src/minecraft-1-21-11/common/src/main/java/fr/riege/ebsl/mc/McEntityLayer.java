package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.domain.entity.EntitySnapshot;
import fr.riege.ebsl.common.platform.layer.IEntityLayer;
import fr.riege.ebsl.common.math.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class McEntityLayer implements IEntityLayer {
    private final Minecraft client;

    public McEntityLayer(Minecraft client) {
        this.client = client;
    }

    @Override
    public List<EntitySnapshot> entitiesForRendering() {
        if (client.level == null) return List.of();
        List<EntitySnapshot> out = new ArrayList<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            out.add(snapshot(entity));
        }
        return out;
    }

    private static EntitySnapshot snapshot(Entity entity) {
        Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        AABB box = entity.getBoundingBox();
        boolean living = entity instanceof LivingEntity;
        float health = living ? ((LivingEntity) entity).getHealth() : 0.0f;
        var eye = entity.getEyePosition();
        return new EntitySnapshot(
            entity.getId(),
            typeId == null ? "" : typeId.toString(),
            entity.getDisplayName().getString(),
            entity.getName().getString(),
            new Vec3d(entity.getX(), entity.getY(), entity.getZ()),
            new Vec3d(eye.x, eye.y, eye.z),
            box.minX,
            box.minY,
            box.minZ,
            box.maxX,
            box.maxY,
            box.maxZ,
            living,
            entity instanceof Mob,
            entity.isAlive(),
            entity.isRemoved(),
            health);
    }
}
