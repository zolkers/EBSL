package fr.riege.ebsl.common.api.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes metadata for {@code EbslApiSurface} declarations.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EbslApiSurface {
    Domain value();

    enum Domain {
        CORE,
        NAVIGATION,
        PATHFINDING,
        RUNTIME,
        SETTINGS,
        MODULES,
        EVENTS,
        ANALYTICS,
        RENDERING,
        THREADING,
        UI
    }
}
