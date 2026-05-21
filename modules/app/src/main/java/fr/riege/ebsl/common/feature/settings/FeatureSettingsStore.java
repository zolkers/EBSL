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

package fr.riege.ebsl.common.feature.settings;

import fr.riege.ebsl.common.core.settings.CommonSettingsStore;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.registry.FeatureRegistries;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;

public final class FeatureSettingsStore {
    private FeatureSettingsStore() {
    }

    public static void load(IStorageLayer storage) {
        CommonSettingsStore.loadGroupedSettings(
            storage,
            CommonSettingsStore.MODULES_KEY,
            FeatureRegistries.modules().all(),
            PathfinderModule::id,
            PathfinderModule::settings,
            FeatureRegistries.modules()::syncLifecycle);
        CommonSettingsStore.loadGroupedSettings(
            storage,
            CommonSettingsStore.TASKS_KEY,
            FeatureRegistries.tasks().all(),
            BotTask::id,
            BotTask::settings,
            FeatureRegistries.tasks()::syncLifecycle);
    }

    public static void save(IStorageLayer storage) {
        CommonSettingsStore.saveGroupedSettings(
            storage,
            CommonSettingsStore.MODULES_KEY,
            FeatureRegistries.modules().all(),
            PathfinderModule::id,
            PathfinderModule::settings);
        CommonSettingsStore.saveGroupedSettings(
            storage,
            CommonSettingsStore.TASKS_KEY,
            FeatureRegistries.tasks().all(),
            BotTask::id,
            BotTask::settings);
    }
}
