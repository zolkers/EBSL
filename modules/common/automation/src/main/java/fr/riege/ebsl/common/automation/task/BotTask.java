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

import java.util.List;

/**
 * Describes a toggleable automation task hosted by the application layer.
 *
 * <p>Tasks expose settings and lifecycle hooks while the registry owns discovery, ticking, rendering, and disable semantics.</p>
 */
public interface BotTask {
    /**
     * Returns the stable identifier used for lookup, persistence, and diagnostics.
 *
     * @return the value defined by this contract
     */
    String id();
    /**
     * Returns the human-readable name shown in UI and help surfaces.
 *
     * @return the value defined by this contract
     */
    String displayName();
    /**
     * Returns a concise human-readable description of this component.
 *
     * @return the value defined by this contract
     */
    String description();
    /**
     * Returns whether this component is currently enabled.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isEnabled();
    /**
     * Updates whether this component is enabled.
 *
     * @param enabled whether the component should be enabled
     */
    void setEnabled(boolean enabled);
    /**
     * Returns the mutable settings exposed by this component.
 *
     * @return the requested values
     */
    List<Setting<?>> settings();
    /**
     * Restores every setting owned by this component to its default value.
     */
    void resetSettings();

    /**
     * Advances this component by one runtime tick.
 *
     * @param platform the platform services available to the component
     */
    default void tick(EbslPlatform platform) {}
    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @param platform the platform services available to the component
     */
    default void render(EbslPlatform platform) {}
    /**
     * Advances this component by one runtime tick.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean tickAsync() { return false; }
    /**
     * Handles cleanup immediately before or after the component is disabled.
     */
    default void onDisable() {}
    /**
     * Handles notification that one of the component settings changed.
 *
     * @param setting the setting being rendered or updated
     */
    default void onSettingChanged(Setting<?> setting) {}
}
