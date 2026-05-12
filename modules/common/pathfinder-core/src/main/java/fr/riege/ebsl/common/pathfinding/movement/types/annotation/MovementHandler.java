package fr.riege.ebsl.common.pathfinding.movement.types.annotation;

import fr.riege.ebsl.common.pathfinding.Node;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface MovementHandler {
    Node.MoveType value();
}
