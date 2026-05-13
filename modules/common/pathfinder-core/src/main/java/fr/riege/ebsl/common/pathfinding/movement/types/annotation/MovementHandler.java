package fr.riege.ebsl.common.pathfinding.movement.types.annotation;

import fr.riege.ebsl.common.pathfinding.Node;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the evaluator or executor responsible for a movement type.
 *
 * <p>Registries use the annotation to bind movement-specific implementations to their {@code Node.MoveType}.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface MovementHandler {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
    Node.MoveType value();
}
