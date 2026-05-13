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

abstract class AbstractSetting<T> extends Settingable implements Setting<T> {
    private final String id;
    private final String displayName;
    private final T defaultValue;
    private T value;

    AbstractSetting(String id, String displayName, T defaultValue) {
        this.id = id;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
        onSettingChanged(this);
    }

    @Override
    public void resetToDefault() {
        setValue(defaultValue);
    }
}
