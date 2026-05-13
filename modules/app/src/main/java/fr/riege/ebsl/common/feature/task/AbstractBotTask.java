package fr.riege.ebsl.common.feature.task;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.Settingable;

public abstract class AbstractBotTask extends Settingable implements BotTask {
    private final String id;
    private final String displayName;
    private final String description;
    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));

    protected AbstractBotTask(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final String displayName() {
        return displayName;
    }

    @Override
    public final String description() {
        return description;
    }

    @Override
    public final boolean isEnabled() {
        return enabledSetting.value();
    }

    @Override
    public final void setEnabled(boolean enabled) {
        enabledSetting.setValue(enabled);
    }
}
