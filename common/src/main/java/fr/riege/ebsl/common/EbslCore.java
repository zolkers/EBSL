package fr.riege.ebsl.common;

import fr.riege.ebsl.common.platform.EbslPlatform;

public class EbslCore {
    private final EbslPlatform platform;

    public EbslCore(EbslPlatform platform) {
        this.platform = platform;
        // subsystems wired here as they migrate to common (Phase 4)
    }
}
