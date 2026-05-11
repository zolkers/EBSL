package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.core.settings.Setting;
import java.util.List;
import java.util.stream.IntStream;

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

    default List<EbslNodeField> fields() {
        List<Setting<?>> settings = settings();
        return IntStream.range(0, settings.size())
            .mapToObj(index -> EbslNodeField.fromSetting(id(), index, settings.get(index)))
            .toList();
    }

    default void loadArgs(List<String> args) {
    }

    default String argsFromSettings() {
        return "";
    }

    int start(EbslNodeInvocation invocation);

    default void tick(EbslNodeInvocation invocation) {
    }

    default boolean isComplete(EbslNodeInvocation invocation) {
        return false;
    }

    default void finish(EbslNodeInvocation invocation) {
    }
}
