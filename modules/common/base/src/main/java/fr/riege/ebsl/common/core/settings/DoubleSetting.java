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

public final class DoubleSetting extends AbstractSetting<Double> {
    private final double min;
    private final double max;

    public DoubleSetting(String id, String displayName, double defaultValue, double min, double max) {
        super(id, displayName, defaultValue);
        this.min = min;
        this.max = max;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    @Override
    public void setValue(Double value) {
        super.setValue(Math.clamp(value, min, max));
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value());
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            setValue(json.getAsDouble());
        }
    }
}
