package fr.riege.ebsl.common.feature.scripting;

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

    int start(EbslNodeInvocation invocation);

    default void finish(EbslNodeInvocation invocation) {
    }
}
