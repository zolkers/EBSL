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

public final class ScaledMousePosEvent extends Event {
    public enum Axis { X, Y }

    private final double rawPos;
    private double scaledPos;
    private final Axis axis;

    public ScaledMousePosEvent(double rawPos, double scaledPos, Axis axis) {
        this.rawPos = rawPos;
        this.scaledPos = scaledPos;
        this.axis = axis;
    }

    public double rawPos() { return rawPos; }
    public double scaledPos() { return scaledPos; }
    public void setScaledPos(double scaledPos) { this.scaledPos = scaledPos; }
    public Axis axis() { return axis; }
}
