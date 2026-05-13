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
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EnumSetting<E extends Enum<E>> extends AbstractSetting<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-settings");

    private final Class<E> type;

    public EnumSetting(String id, String displayName, E defaultValue, Class<E> type) {
        super(id, displayName, defaultValue);
        this.type = type;
    }

    public Class<E> enumType() {
        return type;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value().name());
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        try {
            setValue(Enum.valueOf(type, json.getAsString()));
        } catch (IllegalArgumentException exception) {
            LOGGER.debug("Ignoring invalid enum setting '{}': {}", id(), json, exception);
        }
    }
}
