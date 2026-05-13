package fr.riege.ebsl.common.pathfinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents a path state transition made by execution code.
 *
 * <p>The annotation makes transition intent visible to reviewers and analysis tools.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PathStateTransition {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
    Action value();

    String reason() default "";

    enum Action {
        BEGIN,
        PRESERVE,
        RESET,
        REPLACE,
        MERGE,
        CLEAR
    }
}
