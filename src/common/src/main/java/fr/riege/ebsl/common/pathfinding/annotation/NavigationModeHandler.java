package fr.riege.ebsl.common.pathfinding.annotation;

import fr.riege.ebsl.common.pathfinding.NavigationMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface NavigationModeHandler {
    NavigationMode value();
}
