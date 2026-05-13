package fr.riege.ebsl.common.feature.module;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.layer.IEventBus;
import fr.riege.ebsl.common.platform.service.NavigationService;

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
    default boolean renderGameViewportAsync() { return false; }
}
