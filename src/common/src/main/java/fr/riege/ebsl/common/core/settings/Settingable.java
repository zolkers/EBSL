package fr.riege.ebsl.common.core.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Settingable {
    private final List<Setting<?>> settings = new ArrayList<>();

    protected final <T extends Setting<?>> T registerSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }

    public Setting<?> settingById(String id) {
        for (Setting<?> setting : settings) {
            if (setting.id().equals(id)) {
                return setting;
            }
        }
        return null;
    }

    public void resetSettings() {
        for (Setting<?> setting : settings) {
            setting.resetToDefault();
        }
    }

    public void onSettingChanged(Setting<?> setting) {
    }
}
