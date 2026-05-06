package fr.riege.ebsl.common.module.blacklist;

import fr.riege.ebsl.common.world.BlockId;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class BlockBlacklist {
    private static final AtomicReference<Set<BlockId>> IDS = new AtomicReference<>(Set.of());
    private static volatile boolean enabled;

    private BlockBlacklist() {
    }

    public static void update(boolean enabled, Set<BlockId> ids) {
        BlockBlacklist.enabled = enabled;
        IDS.set(ids == null ? Set.of() : Set.copyOf(ids));
    }

    public static boolean isBlacklisted(BlockId id) {
        return enabled && id != null && IDS.get().contains(id);
    }
}
