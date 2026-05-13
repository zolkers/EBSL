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

package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

record PathVisualizerSnapshot(List<Node> path, List<Vec3d> cameraPath, int cameraRailIndex,
                              double cameraRailVisualProgress, Vec3d cameraRailVisualPosition) {
    PathVisualizerSnapshot {
        path = List.copyOf(path);
        cameraPath = List.copyOf(cameraPath);
    }
}
