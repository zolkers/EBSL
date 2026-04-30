package fr.riege.ebsl.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EbslApiSurface {
    Domain value();

    enum Domain {
        CORE,
        NAVIGATION,
        SETTINGS,
        MODULES,
        TASKS,
        EVENTS,
        ANALYTICS,
        UI
    }
}
