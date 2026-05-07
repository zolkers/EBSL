package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.platform.service.UiService;

public final class ModloaderUiService implements UiService {
    private boolean visible;

    @Override public boolean toggle() {
        visible = !visible;
        return visible;
    }

    @Override public boolean isVisible() {
        return visible;
    }
}
