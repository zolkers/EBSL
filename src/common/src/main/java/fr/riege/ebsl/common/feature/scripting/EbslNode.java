package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.core.settings.Setting;
import java.util.List;

public interface EbslNode {
    String id();

    default List<String> aliases() {
        return List.of();
    }

    default boolean waitsForNavigation() {
        return false;
    }

    default boolean releasesGameplayKeys() {
        return false;
    }

    default boolean isWaitUntil() {
        return false;
    }

    default List<Setting<?>> settings() {
        return List.of();
    }

    default void loadArgs(List<String> args) {
    }

    default String argsFromSettings() {
        return "";
    }

    int start(EbslNodeInvocation invocation);

    default void finish(EbslNodeInvocation invocation) {
    }
}
