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

package fr.riege.ebsl.common.feature.scripting.enums;

import fr.riege.ebsl.common.platform.layer.IInputLayer;
import java.util.Arrays;
import java.util.Locale;

public enum EbslInputKey {
    FORWARD("forward", "w") {
        @Override public void set(IInputLayer input, boolean down) { input.setForwardDown(down); }
        @Override public boolean isDown(IInputLayer input) { return input.forwardDown(); }
    },
    BACKWARD("backward", "back", "s") {
        @Override public void set(IInputLayer input, boolean down) { input.setBackwardDown(down); }
        @Override public boolean isDown(IInputLayer input) { return input.backwardDown(); }
    },
    LEFT("left", "a") {
        @Override public void set(IInputLayer input, boolean down) { input.setLeftDown(down); }
        @Override public boolean isDown(IInputLayer input) { return input.leftDown(); }
    },
    RIGHT("right", "d") {
        @Override public void set(IInputLayer input, boolean down) { input.setRightDown(down); }
        @Override public boolean isDown(IInputLayer input) { return input.rightDown(); }
    },
    JUMP("jump", "space") {
        @Override public void set(IInputLayer input, boolean down) { input.setJumpDown(down); }
        @Override public boolean isDown(IInputLayer input) { return input.jumpDown(); }
    },
    SNEAK("sneak", "shift", "crouch") {
        @Override public void set(IInputLayer input, boolean down) { input.setSneakDown(down); }
        @Override public boolean isDown(IInputLayer input) { return input.sneakDown(); }
    },
    SPRINT("sprint") {
        @Override public void set(IInputLayer input, boolean down) { input.setSprintDown(down); }
    },
    ATTACK("attack", "break") {
        @Override public void set(IInputLayer input, boolean down) { input.setAttackDown(down); }
    },
    USE("use", "interact") {
        @Override public void set(IInputLayer input, boolean down) { input.setUseDown(down); }
    };

    private final String[] aliases;

    EbslInputKey(String... aliases) {
        this.aliases = aliases;
    }

    public abstract void set(IInputLayer input, boolean down);

    @SuppressWarnings("java:S1172")
    public boolean isDown(IInputLayer input) {
        return false;
    }

    public static EbslInputKey byToken(String token) {
        String normalized = normalize(token);
        return Arrays.stream(values())
            .filter(key -> key.matches(normalized))
            .findFirst()
            .orElse(null);
    }

    private boolean matches(String normalized) {
        if (name().toLowerCase(Locale.ROOT).equals(normalized)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
