package fr.riege.ebsl.common.domain.world;

import fr.riege.ebsl.common.core.registry.MapRegistry;

import java.util.Locale;

public final class BlockGroupRegistry {
    private static final MapRegistry<String, BlockMatcher> GROUPS = new MapRegistry<>(null);

    static {
        register(BlockGroupType.LEAF, BlockGroupRegistry::isLeaf);
        register(BlockGroupType.WOOD, BlockGroupRegistry::isWood);
        register(BlockGroupType.GRASS, BlockGroupRegistry::isGrass);
    }

    private BlockGroupRegistry() {
    }

    public static boolean matches(BlockId id, String token) {
        BlockMatcher matcher = GROUPS.get(normalize(token));
        return matcher != null && matcher.matches(id);
    }

    private static void register(BlockGroupType type, BlockMatcher matcher) {
        for (String token : type.tokens()) {
            GROUPS.register(normalize(token), matcher);
        }
    }

    private static boolean isLeaf(BlockId id) {
        String path = path(id);
        return path.endsWith("_leaves") || path.equals("azalea_leaves") || path.equals("flowering_azalea_leaves");
    }

    private static boolean isWood(BlockId id) {
        String path = path(id);
        return path.endsWith("_log")
            || path.endsWith("_wood")
            || path.endsWith("_stem")
            || path.endsWith("_hyphae")
            || path.endsWith("_stripped_log")
            || path.endsWith("_stripped_wood")
            || path.endsWith("_stripped_stem")
            || path.endsWith("_stripped_hyphae")
            || path.startsWith("stripped_");
    }

    private static boolean isGrass(BlockId id) {
        String path = path(id);
        return path.equals("grass")
            || path.equals("short_grass")
            || path.equals("tall_grass")
            || path.equals("fern")
            || path.equals("large_fern")
            || path.equals("seagrass")
            || path.equals("tall_seagrass");
    }

    private static String path(BlockId id) {
        return id == null ? "" : id.path().toLowerCase(Locale.ROOT);
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    @FunctionalInterface
    private interface BlockMatcher {
        boolean matches(BlockId id);
    }
}
