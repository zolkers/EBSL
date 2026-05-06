package fr.riege.ebsl.common.module;

import fr.riege.ebsl.common.layer.IEventBus;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.settings.Setting;
import fr.riege.ebsl.common.ui.layout.UiRect;

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

    default void onEnable(IEventBus bus) {}
    default void onDisable() {}
    default void onSettingChanged(Setting<?> setting) {}
    default void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {}
}
