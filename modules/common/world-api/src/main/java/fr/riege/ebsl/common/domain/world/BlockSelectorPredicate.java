package fr.riege.ebsl.common.domain.world;

@FunctionalInterface
public interface BlockSelectorPredicate {
    boolean matches(BlockId id);
}
