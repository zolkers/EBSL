package fr.riege.ebsl.general.task;

import fr.riege.ebsl.settings.Setting;
import net.minecraft.client.Minecraft;

import java.util.List;

public interface BotTask {
    String id();

    String displayName();

    String description();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    List<Setting<?>> settings();

    void resetSettings();

    default void tick(Minecraft mc) {}

    default void render(Minecraft mc) {}

    default void onDisable() {}

    default void onSettingChanged(Setting<?> setting) {}
}
