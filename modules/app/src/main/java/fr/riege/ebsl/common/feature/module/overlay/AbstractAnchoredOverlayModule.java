package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.feature.AbstractEnabledFeature;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.module.PathfinderModuleCategory;

abstract class AbstractAnchoredOverlayModule extends AbstractEnabledFeature implements PathfinderModule {
    private final EnumSetting<KeyDisplayAnchor> anchorSetting;

    AbstractAnchoredOverlayModule(String id, String displayName, String description, KeyDisplayAnchor defaultAnchor) {
        super(id, displayName, description);
        anchorSetting = registerSetting(new EnumSetting<>("anchor", "Position", defaultAnchor, KeyDisplayAnchor.class));
    }

    @Override
    public final PathfinderModuleCategory category() {
        return PathfinderModuleCategory.RENDER;
    }

    final KeyDisplayAnchor anchor() {
        return anchorSetting.value();
    }
}
