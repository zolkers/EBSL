package fr.riege.ebsl.common.pathfinding.annotation;

import fr.riege.ebsl.common.pathfinding.NavigationMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a navigation handler for a specific navigation mode.
 *
 * <p>Discovery code uses the annotation to connect mode declarations to implementations without hardcoded class wiring.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface NavigationModeHandler {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
    NavigationMode value();
}
