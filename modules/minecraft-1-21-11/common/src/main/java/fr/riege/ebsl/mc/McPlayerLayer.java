package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.domain.world.TargetedBlock;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

final class McPlayerLayer implements IPlayerLayer {
    private final Minecraft client;
    McPlayerLayer(Minecraft client) { this.client = client; }

    @Override public Vec3d position() {
        LocalPlayer player = player();
        return player == null ? zero() : new Vec3d(player.getX(), player.getY(), player.getZ());
    }

    @Override public Vec3d velocity() {
        LocalPlayer player = player();
        if (player == null) return zero();
        return new Vec3d(player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);
    }

    @Override public Vec3d eyePosition() {
        LocalPlayer player = player();
        return player == null ? zero() : new Vec3d(player.getX(), player.getEyeY(), player.getZ());
    }

    @Override public float yaw() {
        LocalPlayer player = player();
        return player == null ? 0.0f : player.getYRot();
    }

    @Override public float pitch() {
        LocalPlayer player = player();
        return player == null ? 0.0f : player.getXRot();
    }

    @Override public boolean onGround() {
        LocalPlayer player = player();
        return player != null && player.onGround();
    }

    @Override public boolean isFlying() {
        LocalPlayer player = player();
        return player != null && player.getAbilities().flying;
    }

    @Override public boolean isInWater() {
        LocalPlayer player = player();
        return player != null && player.isInWater();
    }

    @Override public boolean isInLava() {
        LocalPlayer player = player();
        return player != null && player.isInLava();
    }

    @Override public boolean isSprinting() {
        LocalPlayer player = player();
        return player != null && player.isSprinting();
    }

    @Override public boolean isAlive() {
        LocalPlayer player = player();
        return player != null && player.isAlive();
    }

    @Override public float getHealth() {
        LocalPlayer player = player();
        return player == null ? 0.0f : player.getHealth();
    }

    @Override public TargetedBlock targetedBlockHit() {
        if (client.level == null || !(client.hitResult instanceof BlockHitResult hit)) return null;
        if (hit.getType() == HitResult.Type.MISS) return null;
        var pos = hit.getBlockPos();
        var id = BuiltInRegistries.BLOCK.getKey(client.level.getBlockState(pos).getBlock());
        return new TargetedBlock(pos.getX(), pos.getY(), pos.getZ(), new BlockId(id.getNamespace(), id.getPath()));
    }

    @Override public Integer entityId() {
        LocalPlayer player = player();
        return player == null ? null : player.getId();
    }

    private LocalPlayer player() {
        return client.player;
    }

    private static Vec3d zero() {
        return new Vec3d(0.0, 0.0, 0.0);
    }
}
