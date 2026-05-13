/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.core.settings;

import com.google.gson.JsonElement;

/**
 * Represents one configurable value exposed by modules, tasks, or UI components.
 *
 * <p>Settings carry identity, display metadata, current/default values, and JSON persistence behavior.</p>
 */
public interface Setting<T> {
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

    T value();

    T defaultValue();

    /**
     * Updates the current setting value.
 *
     * @param value the value to apply
     */
    void setValue(T value);

    /**
     * Restores this setting to its default value.
     */
    void resetToDefault();

    /**
     * Serializes this setting to a JSON value.
 *
     * @return the value defined by this contract
     */
    JsonElement toJson();

    /**
     * Loads this setting from a JSON value.
 *
     * @param json the serialized JSON payload
     */
    void load(JsonElement json);
}
