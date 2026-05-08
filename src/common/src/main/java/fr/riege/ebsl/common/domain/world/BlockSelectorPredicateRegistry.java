package fr.riege.ebsl.common.domain.world;

import fr.riege.ebsl.common.core.registry.MapRegistry;

import java.util.Locale;

public final class BlockSelectorPredicateRegistry {
    private static final MapRegistry<String, BlockSelectorPredicate> PREDICATES = new MapRegistry<>(null);

    static {
        register(BlockGroupType.LEAF, BlockGroupRegistry::isLeaf);
        register(BlockGroupType.WOOD, BlockGroupRegistry::isWood);
        register(BlockGroupType.GRASS, BlockGroupRegistry::isGrass);
    }

    private BlockSelectorPredicateRegistry() {
    }

    public static boolean matches(BlockId id, String token) {
        BlockSelectorPredicate predicate = PREDICATES.get(normalize(token));
        return predicate != null && predicate.matches(id);
    }

    public static void register(String token, BlockSelectorPredicate predicate) {
        PREDICATES.register(normalize(token), predicate);
    }

    private static void register(BlockGroupType type, BlockSelectorPredicate predicate) {
        for (String token : type.tokens()) {
            register(token, predicate);
        }
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
