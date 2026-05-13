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

public enum EbslDurationUnit {
    MILLISECOND("ms"),
    SECOND("s"),
    TICK("t");

    private final String suffix;

    EbslDurationUnit(String suffix) {
        this.suffix = suffix;
    }

    public static EbslDurationUnit fromToken(String token) {
        if (token == null) {
            return null;
        }
        for (EbslDurationUnit unit : values()) {
            if (token.endsWith(unit.suffix)) {
                return unit;
            }
        }
        return null;
    }

    public static boolean hasDurationSuffix(String token) {
        EbslDurationUnit unit = fromToken(token);
        if (unit == null) {
            return false;
        }
        try {
            unit.numericValue(token);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public double numericValue(String token) {
        return Double.parseDouble(token.substring(0, token.length() - suffix.length()));
    }

    public int toTicks(double value) {
        return switch (this) {
            case MILLISECOND -> Math.max(1, (int) Math.ceil(value / 50.0));
            case SECOND -> Math.max(1, (int) Math.ceil(value * 20.0));
            case TICK -> Math.max(0, (int) Math.floor(value));
        };
    }
}
