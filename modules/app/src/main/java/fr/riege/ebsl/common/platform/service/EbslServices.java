package fr.riege.ebsl.common.platform.service;

import fr.riege.ebsl.common.platform.EbslPlatform;

import java.util.Objects;

public final class EbslServices {
    private static EbslPlatform platform;
    private static NavigationService navigation;
    private static UiService ui;

    private EbslServices() {
    }

    public static void install(NavigationService navigationService, UiService uiService) {
        navigation = Objects.requireNonNull(navigationService, "navigationService");
        ui = Objects.requireNonNull(uiService, "uiService");
    }

    public static void installPlatform(EbslPlatform value) {
        platform = Objects.requireNonNull(value, "platform");
    }

    public static EbslPlatform platform() {
        if (platform == null) {
            throw new IllegalStateException("EbslPlatform has not been installed");
        }
        return platform;
    }

    public static NavigationService navigation() {
        if (navigation == null) {
            throw new IllegalStateException("NavigationService has not been installed");
        }
        return navigation;
    }

    public static boolean isNavigationActive() {
        return navigation != null && navigation.isNavigating();
    }

    public static UiService ui() {
        if (ui == null) {
            throw new IllegalStateException("UiService has not been installed");
        }
        return ui;
    }
}
