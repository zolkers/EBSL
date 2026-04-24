package fr.riege.ebsl.botting.module;

import fr.riege.ebsl.settings.Setting;
import java.util.List;

public interface BotModule {
    String id();

    String displayName();

    String description();

    BotModuleCategory category();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    List<Setting<?>> settings();

    void resetSettings();
}
