package fr.riege.ebsl.common.core.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Settingable {
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean settingsRegistered;

    protected final <T extends Setting<?>> T registerSetting(T setting) {
        if (findSetting(setting.id()) != null) {
            throw new IllegalStateException("Duplicate setting id: " + setting.id());
        }
        settings.add(setting);
        return setting;
    }

    protected void registerSettings() {
    }

    public List<Setting<?>> settings() {
        ensureSettingsRegistered();
        return Collections.unmodifiableList(settings);
    }

    @SuppressWarnings("java:S1452")
    public Setting<?> settingById(String id) {
        ensureSettingsRegistered();
        return findSetting(id);
    }

    public void resetSettings() {
        ensureSettingsRegistered();
        for (Setting<?> setting : settings) {
            setting.resetToDefault();
        }
    }

    public void onSettingChanged(Setting<?> setting) {
    }

    private void ensureSettingsRegistered() {
        if (settingsRegistered) {
            return;
        }
        settingsRegistered = true;
        registerSettings();
    }

    private Setting<?> findSetting(String id) {
        for (Setting<?> setting : settings) {
            if (setting.id().equals(id)) {
                return setting;
            }
        }
        return null;
    }
}
