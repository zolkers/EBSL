package fr.riege.ebsl.common.feature;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.Settingable;

public abstract class AbstractEnabledFeature extends Settingable {
    private final String id;
    private final String displayName;
    private final String description;
    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));

    protected AbstractEnabledFeature(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public final String id() {
        return id;
    }

    public final String displayName() {
        return displayName;
    }

    public final String description() {
        return description;
    }

    public final boolean isEnabled() {
        return enabledSetting.value();
    }

    public final void setEnabled(boolean enabled) {
        enabledSetting.setValue(enabled);
    }
}
