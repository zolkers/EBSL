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

package fr.riege.ebsl.common.automation.task;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.platform.EbslPlatform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotTaskRegistryTest {
    @Test
    void registersAndLooksUpTasks() {
        TestTask task = new TestTask("registry_lookup_task");

        BotTaskRegistry.registerIfAbsent(task);
        BotTaskRegistry.registerIfAbsent(new TestTask("registry_lookup_task"));

        assertSame(task, BotTaskRegistry.get("registry_lookup_task"));
        assertTrue(BotTaskRegistry.tasks().contains(task));
    }

    @Test
    void ticksAndRendersOnlyEnabledTasks() {
        TestTask task = new TestTask("registry_enabled_task");
        BotTaskRegistry.registerIfAbsent(task);

        BotTaskRegistry.update(null);
        BotTaskRegistry.render(null);
        task.setEnabled(true);
        BotTaskRegistry.update(null);
        BotTaskRegistry.render(null);

        assertEquals(1, task.ticks);
        assertEquals(1, task.renders);
    }

    @Test
    void notifiesSettingsAndDisableLifecycle() {
        TestTask task = new TestTask("registry_lifecycle_task");
        BotTaskRegistry.registerIfAbsent(task);

        task.setEnabled(true);
        BotTaskRegistry.syncLifecycle(task);
        task.setEnabled(false);
        BotTaskRegistry.syncLifecycle(task);
        BotTaskRegistry.notifySettingChanged(task, null);
        BotTaskRegistry.resetToDefaultsAndSave(task);
        BotTaskRegistry.saveSettings();

        assertEquals(1, task.disables);
        assertEquals(1, task.settingNotifications);
        assertEquals(1, task.resets);
    }

    private static final class TestTask extends AbstractBotTask {
        private int ticks;
        private int renders;
        private int disables;
        private int resets;
        private int settingNotifications;

        private TestTask(String id) {
            super(id, id, "Test task");
        }

        @Override
        public void tick(EbslPlatform platform) {
            ticks++;
        }

        @Override
        public void render(EbslPlatform platform) {
            renders++;
        }

        @Override
        public void onDisable() {
            disables++;
        }

        @Override
        public void resetSettings() {
            resets++;
        }

        @Override
        public void onSettingChanged(Setting<?> setting) {
            settingNotifications++;
        }
    }
}
