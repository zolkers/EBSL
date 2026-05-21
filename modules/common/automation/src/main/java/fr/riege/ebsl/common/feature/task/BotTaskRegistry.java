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

package fr.riege.ebsl.common.feature.task;

import fr.riege.ebsl.common.core.registry.IRegistry;
import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.threading.EbslThreadDomain;
import fr.riege.ebsl.common.core.threading.EbslThreading;
import fr.riege.ebsl.common.platform.EbslPlatform;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BotTaskRegistry {
    private static final IRegistry<String, BotTask> TASKS = new MapRegistry<>(null);
    private static final Map<String, Boolean> lastEnabled = new HashMap<>();
    private static final Set<String> runningAsyncTicks = ConcurrentHashMap.newKeySet();

    private BotTaskRegistry() {}

    public static void register(BotTask task) {
        TASKS.register(task.id(), task);
        lastEnabled.put(task.id(), task.isEnabled());
    }

    public static void bootstrap() {
        registerIfAbsent(SpaceMobTask.INSTANCE);
    }

    public static void registerIfAbsent(BotTask task) {
        if (TASKS.get(task.id()) == null) {
            register(task);
        }
    }

    public static void update(EbslPlatform platform) {
        for (BotTask task : TASKS.values()) {
            syncLifecycle(task);
            if (task.isEnabled()) {
                tickTask(platform, task);
            }
        }
    }

    private static void tickTask(EbslPlatform platform, BotTask task) {
        if (!task.tickAsync()) {
            try {
                task.tick(platform);
            } catch (RuntimeException exception) {
                EbslThreading.report(EbslThreadDomain.TASKS, task.id() + ".tick", exception);
            }
            return;
        }
        if (!runningAsyncTicks.add(task.id())) {
            return;
        }
        EbslThreading.tasks()
            .run(task.id() + ".tick", () -> task.tick(platform))
            .whenComplete((unused, throwable) -> runningAsyncTicks.remove(task.id()));
    }

    public static void render(EbslPlatform platform) {
        for (BotTask task : TASKS.values()) {
            if (task.isEnabled()) {
                try {
                    task.render(platform);
                } catch (RuntimeException exception) {
                    EbslThreading.report(EbslThreadDomain.TASKS, task.id() + ".render", exception);
                }
            }
        }
    }

    public static void onSettingChanged(BotTask task, Setting<?> setting) {
        task.onSettingChanged(setting);
    }

    public static void saveSettings() {
        // Settings are currently owned by each task instance; registry persistence is intentionally inert.
    }

    public static void notifySettingChanged(BotTask task, Setting<?> setting) {
        onSettingChanged(task, setting);
    }

    public static void resetToDefaultsAndSave(BotTask task) {
        task.resetSettings();
    }

    public static void syncLifecycle(BotTask task) {
        boolean isEnabled = task.isEnabled();
        Boolean was = lastEnabled.put(task.id(), isEnabled);
        if (was != null && was != isEnabled && !isEnabled) {
            try {
                task.onDisable();
            } catch (RuntimeException exception) {
                EbslThreading.report(EbslThreadDomain.TASKS, task.id() + ".onDisable", exception);
            }
        }
    }

    public static Collection<BotTask> tasks() {
        return TASKS.values();
    }

    public static BotTask get(String id) {
        return TASKS.get(id);
    }
}
