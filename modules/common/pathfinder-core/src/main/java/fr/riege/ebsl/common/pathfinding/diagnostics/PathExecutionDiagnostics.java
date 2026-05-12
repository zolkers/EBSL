package fr.riege.ebsl.common.pathfinding.diagnostics;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

public final class PathExecutionDiagnostics {
    private static final Sink NOOP = new Sink() {
    };

    private static volatile Sink sink = NOOP;

    private PathExecutionDiagnostics() {
    }

    public static void setSink(Sink sink) {
        PathExecutionDiagnostics.sink = sink == null ? NOOP : sink;
    }

    public static void clear() {
        sink.clear();
    }

    public static void setPath(List<Node> path) {
        sink.setPath(path);
    }

    public static void setCameraPath(List<Vec3d> path) {
        sink.setCameraPath(path);
    }

    public static void updateExecution(int cameraTargetIndex) {
        sink.updateExecution(cameraTargetIndex);
    }

    public interface Sink {
        default void clear() {
        }

        default void setPath(List<Node> path) {
        }

        default void setCameraPath(List<Vec3d> path) {
        }

        default void updateExecution(int cameraTargetIndex) {
        }
    }
}
