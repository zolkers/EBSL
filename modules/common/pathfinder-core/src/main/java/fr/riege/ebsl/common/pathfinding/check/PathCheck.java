package fr.riege.ebsl.common.pathfinding.check;

/**
 * Evaluates one runtime path health check.
 *
 * <p>Checks inspect execution context and return structured actions that keep recovery behavior explicit.</p>
 */
interface PathCheck {
    /**
     * Evaluates this contract against the supplied context.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    PathCheckResult evaluate(PathCheckContext context);
}
