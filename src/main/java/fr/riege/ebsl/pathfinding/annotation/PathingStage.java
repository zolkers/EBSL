package fr.riege.ebsl.pathfinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PathingStage {
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
