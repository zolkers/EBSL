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
package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeFieldHelp;

public record EbslNodeField(
    int argumentIndex,
    String id,
    String label,
    String type,
    String defaultValue,
    String description,
    Setting<?> setting
) {
    public static EbslNodeField fromSetting(String command, int argumentIndex, Setting<?> setting) {
        return new EbslNodeField(
            argumentIndex,
            setting.id(),
            setting.displayName(),
            EbslNodeFieldHelp.typeName(setting),
            EbslNodeFieldHelp.value(setting.defaultValue()),
            EbslNodeFieldHelp.description(command, setting),
            setting
        );
    }
}
