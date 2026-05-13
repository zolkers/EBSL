package fr.riege.ebsl.common.feature.terminal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares terminal metadata for a command implementation.
 *
 * <p>The command registry uses this annotation to expose names, help text, usage, and execution scope consistently.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * Returns the command name declared by this annotation.
 *
     * @return the value defined by this contract
     */
    String name();
    String description() default "";
    String usage() default "";
    CommandScope scope() default CommandScope.MC;
}
