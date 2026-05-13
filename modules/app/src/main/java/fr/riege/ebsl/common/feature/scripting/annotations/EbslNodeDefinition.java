package fr.riege.ebsl.common.feature.scripting.annotations;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the script node type implemented by an EBSL node class.
 *
 * <p>The annotation is consumed by node registration and documentation code to bind runtime classes to language-level node names.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EbslNodeDefinition {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
    EbslNodeType value();

    String[] aliases() default {};
}
