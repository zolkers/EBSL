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

package fr.riege.ebsl.common.core.event;

public final class KeyPressEvent extends Event {
    private final long windowHandle;
    private final int keyCode;
    private final int action;
    private final int modifiers;

    public KeyPressEvent(long windowHandle, int keyCode, int action, int modifiers) {
        this.windowHandle = windowHandle;
        this.keyCode = keyCode;
        this.action = action;
        this.modifiers = modifiers;
    }

    public long windowHandle() { return windowHandle; }
    public int keyCode() { return keyCode; }
    public int action() { return action; }
    public int modifiers() { return modifiers; }
}
