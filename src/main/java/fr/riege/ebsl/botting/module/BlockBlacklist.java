package fr.riege.ebsl.botting.module;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public final class BlockBlacklist {
    private static volatile boolean enabled = false;
    private static volatile Set<Identifier> ids = Set.of();

    private BlockBlacklist() {}

    static void update(boolean active, Set<Identifier> blockIds) {
        enabled = active;
        ids = blockIds == null || blockIds.isEmpty() ? Set.of() : Set.copyOf(blockIds);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Set<Identifier> ids() {
        return ids;
    }

    public static boolean isBlacklisted(BlockState state) {
        if (!enabled || state == null || state.isAir()) return false;
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return ids.contains(id);
    }
}
