package fr.riege.ebsl.common.pathfinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the pathfinding stage represented by a component.
 *
 * <p>Stage metadata helps diagnostics and code review connect behavior to planning, validation, execution, or recovery phases.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PathingStage {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
    Stage value();

    enum Stage {
        GRAPH_EVALUATION,
        PATH_POST_PROCESSING,
        PATH_SMOOTHING,
        RESULT_CLASSIFICATION,
        STATE_PERSISTENCE,
        EXECUTION,
        RECOVERY,
        NAVIGATION_SERVICE
    }
}
