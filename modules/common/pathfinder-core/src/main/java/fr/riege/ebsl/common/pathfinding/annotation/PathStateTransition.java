package fr.riege.ebsl.common.pathfinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes metadata for {@code PathStateTransition} declarations.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PathStateTransition {
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
