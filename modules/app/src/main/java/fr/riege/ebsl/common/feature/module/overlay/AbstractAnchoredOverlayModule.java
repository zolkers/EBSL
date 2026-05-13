package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.core.settings.Settingable;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.module.PathfinderModuleCategory;

abstract class AbstractAnchoredOverlayModule extends Settingable implements PathfinderModule {
    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final EnumSetting<KeyDisplayAnchor> anchorSetting;

    AbstractAnchoredOverlayModule(KeyDisplayAnchor defaultAnchor) {
        anchorSetting = registerSetting(new EnumSetting<>("anchor", "Position", defaultAnchor, KeyDisplayAnchor.class));
    }

    @Override
    public final PathfinderModuleCategory category() {
        return PathfinderModuleCategory.RENDER;
    }

    @Override
    public final boolean isEnabled() {
        return enabledSetting.value();
    }

    @Override
    public final void setEnabled(boolean enabled) {
        enabledSetting.setValue(enabled);
    }

    final KeyDisplayAnchor anchor() {
        return anchorSetting.value();
    }
}
