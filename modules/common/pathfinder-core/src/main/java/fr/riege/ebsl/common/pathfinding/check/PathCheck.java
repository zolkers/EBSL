package fr.riege.ebsl.common.pathfinding.check;

/**
 * Defines the contract for {@code PathCheck} implementations.
 */
interface PathCheck {
    PathCheckResult evaluate(PathCheckContext context);
}
