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

package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.*;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractEbslNode extends Settingable implements EbslNode {
    private final String id;
    private final List<String> aliases;

    AbstractEbslNode() {
        EbslNodeDefinition definition = definition(getClass());
        this.id = definition.value().id();
        this.aliases = List.of(definition.aliases());
    }

    AbstractEbslNode(String id) {
        this(id, List.of());
    }

    AbstractEbslNode(String id, List<String> aliases) {
        this.id = id;
        this.aliases = List.copyOf(aliases);
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final List<String> aliases() {
        return aliases;
    }

    @Override
    public void loadArgs(List<String> args) {
        List<Setting<?>> settings = settings();
        for (int i = 0; i < settings.size(); i++) {
            Setting<?> setting = settings.get(i);
            if (i >= args.size()) {
                setting.resetToDefault();
                continue;
            }
            loadSetting(setting, args.get(i));
        }
    }

    @Override
    public String argsFromSettings() {
        List<String> values = new ArrayList<>();
        for (Setting<?> setting : settings()) {
            values.add(settingValue(setting));
        }
        return String.join(" ", values).trim();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void loadSetting(Setting<?> setting, String value) {
        if (setting instanceof StringSetting s) {
            s.setValue(value);
        } else if (setting instanceof IntSetting s) {
            s.setValue(parseInt(value, s.defaultValue()));
        } else if (setting instanceof DoubleSetting s) {
            s.setValue(parseDouble(value, s.defaultValue()));
        } else if (setting instanceof BooleanSetting s) {
            s.setValue(value == null || value.isBlank() ? s.defaultValue() : Boolean.parseBoolean(value));
        } else if (setting instanceof EnumSetting s) {
            if (value == null || value.isBlank()) {
                setting.resetToDefault();
                return;
            }
            try {
                s.setValue(Enum.valueOf(s.enumType(), value.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                setting.resetToDefault();
            }
        }
    }

    private String settingValue(Setting<?> setting) {
        Object value = setting.value();
        if (value instanceof Enum<?> e) {
            return e.name().toLowerCase();
        }
        return String.valueOf(value);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static EbslNodeDefinition definition(Class<?> nodeClass) {
        EbslNodeDefinition definition = nodeClass.getAnnotation(EbslNodeDefinition.class);
        if (definition == null) {
            throw new IllegalStateException("Missing @EbslNodeDefinition on " + nodeClass.getName());
        }
        return definition;
    }
}
