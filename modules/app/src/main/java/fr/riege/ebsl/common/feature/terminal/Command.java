package fr.riege.ebsl.common.feature.terminal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes metadata for {@code Command} declarations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String description() default "";
    String usage() default "";
    CommandScope scope() default CommandScope.MC;
}
