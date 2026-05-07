package fr.riege.ebsl.common.pathfinding.debug;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

record PathVisualizerSnapshot(List<Node> path, List<Vec3d> cameraPath, int cameraRailIndex) {
    PathVisualizerSnapshot {
        path = List.copyOf(path);
        cameraPath = List.copyOf(cameraPath);
    }
}
