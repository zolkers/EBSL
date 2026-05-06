package fr.riege.ebsl.common.pathfinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PathStatePersistence {
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
