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

package fr.riege.ebsl.common.feature.registry;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.feature.task.BotTaskRegistry;
import fr.riege.ebsl.common.platform.EbslPlatform;

import java.util.Collection;

public final class TaskCatalog {
    TaskCatalog() {
    }

    public void bootstrap() {
        BotTaskRegistry.bootstrap();
        BotTaskRegistry.registerIfAbsent(EbslScriptTask.INSTANCE);
    }

    public Collection<BotTask> all() {
        return BotTaskRegistry.tasks();
    }

    public BotTask get(String id) {
        return BotTaskRegistry.get(id);
    }

    public void update(EbslPlatform platform) {
        BotTaskRegistry.update(platform);
    }

    public void render(EbslPlatform platform) {
        BotTaskRegistry.render(platform);
    }

    public void notifySettingChanged(BotTask task, Setting<?> setting) {
        BotTaskRegistry.notifySettingChanged(task, setting);
    }

    public void syncLifecycle(BotTask task) {
        BotTaskRegistry.syncLifecycle(task);
    }

    public void resetToDefaultsAndSave(BotTask task) {
        BotTaskRegistry.resetToDefaultsAndSave(task);
    }
}
