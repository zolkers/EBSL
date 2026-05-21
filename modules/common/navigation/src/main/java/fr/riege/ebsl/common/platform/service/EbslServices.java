/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

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
