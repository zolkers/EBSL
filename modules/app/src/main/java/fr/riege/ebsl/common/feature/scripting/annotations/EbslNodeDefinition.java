package fr.riege.ebsl.common.feature.scripting.annotations;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes metadata for {@code EbslNodeDefinition} declarations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EbslNodeDefinition {
    EbslNodeType value();

    String[] aliases() default {};
}
