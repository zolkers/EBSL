package fr.riege.ebsl.common.domain.world;

/**
 * Defines the contract for {@code BlockSelectorPredicate} implementations.
 */
@FunctionalInterface
public interface BlockSelectorPredicate {
    boolean matches(BlockId id);
}
