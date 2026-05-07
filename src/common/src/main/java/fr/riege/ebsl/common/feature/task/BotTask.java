package fr.riege.ebsl.common.feature.task;

import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.core.settings.Setting;

import java.util.List;

public interface BotTask {
    String id();
    String displayName();
    String description();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    List<Setting<?>> settings();
    void resetSettings();

    default void tick(EbslPlatform platform) {}
    default void render(EbslPlatform platform) {}
    default void onDisable() {}
    default void onSettingChanged(Setting<?> setting) {}
}
