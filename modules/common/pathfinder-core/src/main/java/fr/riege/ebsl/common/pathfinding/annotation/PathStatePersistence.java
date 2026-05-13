package fr.riege.ebsl.common.pathfinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents how path state should persist across execution events.
 *
 * <p>The annotation records whether state survives repairs, restarts, or runtime transitions and why.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PathStatePersistence {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
    Scope value();

    String reason() default "";

    enum Scope {
        REQUEST,
        EXECUTION,
        LONG_RANGE_SESSION,
        VISUALIZATION,
        CONFIGURATION
    }
}
