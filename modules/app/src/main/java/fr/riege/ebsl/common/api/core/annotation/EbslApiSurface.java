package fr.riege.ebsl.common.api.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as part of an exported EBSL API surface.
 *
 * <p>The selected domain is used by API discovery and documentation tooling to group stable entry points.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EbslApiSurface {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
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
