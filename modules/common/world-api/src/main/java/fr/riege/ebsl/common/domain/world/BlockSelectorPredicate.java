package fr.riege.ebsl.common.domain.world;

/**
 * Tests whether a block identifier matches a selector rule.
 *
 * <p>Predicates are pure matching functions used by block selectors and registries.</p>
 */
@FunctionalInterface
public interface BlockSelectorPredicate {
    /**
     * Returns whether the supplied value satisfies this predicate.
 *
     * @param id the block or entity identifier
     * @return true when the condition is satisfied; false otherwise
     */
    boolean matches(BlockId id);
}
