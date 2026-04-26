package fr.riege.ebsl.botting.module;

import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.settings.Setting;

import java.util.List;

public interface PathfinderModule {
    String id();

    String displayName();

    String description();

    PathfinderModuleCategory category();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    List<Setting<?>> settings();

    void resetSettings();

    default void onEnable(EventBus bus) {}

    default void onDisable() {}

    default void onSettingChanged(Setting<?> setting) {}
}
