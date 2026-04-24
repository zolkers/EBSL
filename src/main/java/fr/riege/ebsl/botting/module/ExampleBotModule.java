package fr.riege.ebsl.botting.module;

import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Settingable;

public final class ExampleBotModule extends Settingable implements BotModule {
    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final BooleanSetting avoidVoid = registerSetting(new BooleanSetting("avoid_void", "Avoid void", true));
    private final IntSetting scanRadius = registerSetting(new IntSetting("scan_radius", "Scan radius", 24, 8, 128));

    @Override
    public String id() {
        return "example";
    }

    @Override
    public String displayName() {
        return "Example Module";
    }

    @Override
    public String description() {
        return "Template module showing persistent settings.";
    }

    @Override
    public BotModuleCategory category() {
        return BotModuleCategory.UTILITY;
    }

    @Override
    public boolean isEnabled() {
        return enabledSetting.value();
    }

    @Override
    public void setEnabled(boolean enabled) {
        enabledSetting.setValue(enabled);
    }

}
