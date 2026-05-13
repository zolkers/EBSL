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
package fr.riege.ebsl.common.pathfinding.diagnostics;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class PathExecutionDiagnostics {
    private static final Sink NOOP = new Sink() {
    };

    private static final AtomicReference<Sink> sink = new AtomicReference<>(NOOP);

    private PathExecutionDiagnostics() {
    }

    public static void setSink(Sink sink) {
        PathExecutionDiagnostics.sink.set(sink == null ? NOOP : sink);
    }

    public static void clear() {
        sink.get().clear();
    }

    public static void setPath(List<Node> path) {
        sink.get().setPath(path);
    }

    public static void setCameraPath(List<Vec3d> path) {
        sink.get().setCameraPath(path);
    }

    public static void updateExecution(int cameraTargetIndex) {
        sink.get().updateExecution(cameraTargetIndex);
    }

    /**
     * Defines the sink contract.

     *

     * <p>Implementations provide the stable boundary used by EBSL components that depend on sink behavior.</p>

     */
    public interface Sink {
        /**
         * Clears any state currently held by the receiver.
         */
        default void clear() {
        }

        /**
         * Updates the path snapshot exposed to diagnostics.
 *
         * @param path the path or file path to use
         */
        default void setPath(List<Node> path) {
        }

        /**
         * Updates the camera path snapshot exposed to diagnostics.
 *
         * @param path the path or file path to use
         */
        default void setCameraPath(List<Vec3d> path) {
        }

        /**
         * Updates execution diagnostics with the active camera target index.
 *
         * @param cameraTargetIndex the camera target index value
         */
        default void updateExecution(int cameraTargetIndex) {
        }
    }
}
