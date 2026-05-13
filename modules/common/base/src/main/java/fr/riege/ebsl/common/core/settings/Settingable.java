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
