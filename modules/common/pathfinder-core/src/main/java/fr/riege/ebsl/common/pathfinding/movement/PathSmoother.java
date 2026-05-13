package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.ArrayList;
import java.util.List;


@PathingStage(PathingStage.Stage.PATH_SMOOTHING)
public final class PathSmoother {

    private PathSmoother() {}

    public static List<Node> smooth(List<Node> raw, WalkabilityChecker checker) {
        if (raw == null || raw.size() <= 2) {
            return raw;
        }

        List<Node> result = new ArrayList<>();
        result.add(raw.get(0));

        int anchorIdx = 0;

        while (anchorIdx < raw.size() - 1) {
            Node anchor   = raw.get(anchorIdx);
            int  furthest = anchorIdx + 1;

            int budget = computeAdaptiveSkipBudget(anchor, checker);
            int maxCand = (int) Math.clamp(anchorIdx + (long) budget, 0L, raw.size() - 1L);

            for (int cand = anchorIdx + 2; cand <= maxCand; cand++) {
                Node prevNode  = raw.get(cand - 1);
                Node candidate = raw.get(cand);

                if (!canSmoothToCandidate(raw, anchor, prevNode, candidate, anchorIdx, cand, checker)) {
                    break;
                }
                if (hasLineOfSight(anchor, candidate, checker)) {
                    furthest = cand;
                } else {
                    break;
                }
            }

            anchorIdx = furthest;
            result.add(raw.get(anchorIdx));
        }

        return result;
    }

    private static boolean canSmoothToCandidate(List<Node> raw,
                                                Node anchor,
                                                Node previous,
                                                Node candidate,
                                                int anchorIdx,
                                                int candidateIdx,
                                                WalkabilityChecker checker) {
        return previous.position.flooredY() == anchor.position.flooredY()
            && candidate.position.flooredY() == anchor.position.flooredY()
            && !wouldSkipConstrainedCorner(raw, anchorIdx, candidateIdx, checker);
    }

    public static List<Node> smoothFly(List<Node> raw, WalkabilityChecker checker) {
        if (raw == null || raw.size() <= 2) {
            return raw;
        }

        List<Node> result = new ArrayList<>();
        result.add(raw.getFirst());

        int anchorIdx = 0;
        while (anchorIdx < raw.size() - 1) {
            Node anchor   = raw.get(anchorIdx);
            int  furthest = anchorIdx + 1;

            for (int cand = anchorIdx + 2; cand < raw.size(); cand++) {
                Node candidate = raw.get(cand);
                if (hasFlyLineOfSight(anchor, candidate, checker)) {
                    furthest = cand;
                } else {
                    break;
                }
            }

            anchorIdx = furthest;
            result.add(raw.get(anchorIdx));
        }

        return result;
    }

    private static boolean hasLineOfSight(Node from, Node to, WalkabilityChecker checker) {
        return traceGridLine(from, to, checker, true, (x, y, z) -> clearForSmoothing(checker, x, y, z));
    }

    private static boolean hasFlyLineOfSight(Node from, Node to, WalkabilityChecker checker) {
        return traceGridLine(from, to, checker, false, (x, y, z) -> !checker.isSolid(x, y, z));
    }

    private static boolean traceGridLine(Node from,
                                         Node to,
                                         WalkabilityChecker checker,
                                         boolean blockDiagonalCuts,
                                         CellClearance clearance) {
        GridLine line = GridLine.between(from, to);
        if (line.dx >= line.dy && line.dx >= line.dz) {
            return traceDominantX(line, checker, blockDiagonalCuts, clearance);
        }
        if (line.dy >= line.dx && line.dy >= line.dz) {
            return traceDominantY(line, checker, blockDiagonalCuts, clearance);
        }
        return traceDominantZ(line, checker, blockDiagonalCuts, clearance);
    }

    private static boolean traceDominantX(GridLine line,
                                          WalkabilityChecker checker,
                                          boolean blockDiagonalCuts,
                                          CellClearance clearance) {
        int x = line.x0;
        int y = line.y0;
        int z = line.z0;
        int yError = 2 * line.dy - line.dx;
        int zError = 2 * line.dz - line.dx;
        for (int i = 0; i < line.dx; i++) {
            if (!clearance.clear(x, y, z)) {
                return false;
            }
            int previousX = x;
            int previousZ = z;
            if (yError > 0) {
                y += line.sy;
                yError -= 2 * line.dx;
            }
            if (zError > 0) {
                z += line.sz;
                zError -= 2 * line.dx;
            }
            yError += 2 * line.dy;
            zError += 2 * line.dz;
            x += line.sx;
            if (blocksDiagonalCut(checker, blockDiagonalCuts, previousX, previousZ, x, y, z)) {
                return false;
            }
        }
        return clearance.clear(line.x1, line.y1, line.z1);
    }

    private static boolean traceDominantY(GridLine line,
                                          WalkabilityChecker checker,
                                          boolean blockDiagonalCuts,
                                          CellClearance clearance) {
        int x = line.x0;
        int y = line.y0;
        int z = line.z0;
        int xError = 2 * line.dx - line.dy;
        int zError = 2 * line.dz - line.dy;
        for (int i = 0; i < line.dy; i++) {
            if (!clearance.clear(x, y, z)) {
                return false;
            }
            int previousX = x;
            int previousZ = z;
            if (xError > 0) {
                x += line.sx;
                xError -= 2 * line.dy;
            }
            if (zError > 0) {
                z += line.sz;
                zError -= 2 * line.dy;
            }
            xError += 2 * line.dx;
            zError += 2 * line.dz;
            y += line.sy;
            if (blocksDiagonalCut(checker, blockDiagonalCuts, previousX, previousZ, x, y, z)) {
                return false;
            }
        }
        return clearance.clear(line.x1, line.y1, line.z1);
    }

    private static boolean traceDominantZ(GridLine line,
                                          WalkabilityChecker checker,
                                          boolean blockDiagonalCuts,
                                          CellClearance clearance) {
        int x = line.x0;
        int y = line.y0;
        int z = line.z0;
        int xError = 2 * line.dx - line.dz;
        int yError = 2 * line.dy - line.dz;
        for (int i = 0; i < line.dz; i++) {
            if (!clearance.clear(x, y, z)) {
                return false;
            }
            int previousX = x;
            int previousZ = z;
            if (xError > 0) {
                x += line.sx;
                xError -= 2 * line.dz;
            }
            if (yError > 0) {
                y += line.sy;
                yError -= 2 * line.dz;
            }
            xError += 2 * line.dx;
            yError += 2 * line.dy;
            z += line.sz;
            if (blocksDiagonalCut(checker, blockDiagonalCuts, previousX, previousZ, x, y, z)) {
                return false;
            }
        }
        return clearance.clear(line.x1, line.y1, line.z1);
    }

    private static boolean blocksDiagonalCut(WalkabilityChecker checker,
                                             boolean blockDiagonalCuts,
                                             int previousX,
                                             int previousZ,
                                             int x,
                                             int y,
                                             int z) {
        return blockDiagonalCuts && diagonalStepBlocked(checker, previousX, previousZ, x, y, z);
    }

    private static boolean clearForSmoothing(WalkabilityChecker checker, int x, int y, int z) {
        
        
        
        return checker.isWalkable(x, y, z);
    }

    private static int computeAdaptiveSkipBudget(Node anchor, WalkabilityChecker checker) {
        int wallScore = computeWallScore(anchor, checker);

        if (wallScore <= PathfinderSettings.instance().smoothOpenWallScoreMax.value()) {
            return PathfinderSettings.instance().smoothOpenSkipBudget.value();
        }
        if (wallScore <= PathfinderSettings.instance().smoothMidWallScoreMax.value()) {
            return PathfinderSettings.instance().smoothMidSkipBudget.value();
        }
        return PathfinderSettings.instance().smoothTightSkipBudget.value();
    }

    private static int computeWallScore(Node anchor, WalkabilityChecker checker) {
        if (checker == null) {
            return 0;
        }

        int x = anchor.position.flooredX();
        int y = anchor.position.flooredY();
        int z = anchor.position.flooredZ();

        int wallScore = 0;
        int[] dx = {1, -1, 0, 0, 1, 1, -1, -1};
        int[] dz = {0, 0, 1, -1, 1, -1, 1, -1};
        for (int i = 0; i < dx.length; i++) {
            int nx = x + dx[i];
            int nz = z + dz[i];
            if (checker.isFullWall(nx, y, nz)) {
                wallScore++;
            }
            if (checker.isFullWall(nx, y + 1, nz)) {
                wallScore++;
            }
        }
        return wallScore;
    }

    private static boolean wouldSkipConstrainedCorner(List<Node> raw, int anchorIdx, int candidateIdx,
                                                      WalkabilityChecker checker) {
        for (int i = anchorIdx + 1; i < candidateIdx; i++) {
            if (isConstrainedCorner(raw, i, checker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isConstrainedCorner(List<Node> raw, int idx, WalkabilityChecker checker) {
        Node prev = raw.get(idx - 1);
        Node cur = raw.get(idx);
        Node next = raw.get(idx + 1);

        double inX = cur.position.centeredX() - prev.position.centeredX();
        double inZ = cur.position.centeredZ() - prev.position.centeredZ();
        double outX = next.position.centeredX() - cur.position.centeredX();
        double outZ = next.position.centeredZ() - cur.position.centeredZ();

        double inLen = Math.sqrt(inX * inX + inZ * inZ);
        double outLen = Math.sqrt(outX * outX + outZ * outZ);
        if (inLen < 1.0e-3 || outLen < 1.0e-3) {
            return false;
        }

        double dot = (inX / inLen) * (outX / outLen) + (inZ / inLen) * (outZ / outLen);
        double angleDeg = Math.toDegrees(Math.acos(Math.clamp(dot, -1.0, 1.0)));
        return angleDeg >= PathfinderSettings.instance().smoothConstrainedCornerAngleDeg.value()
            && computeWallScore(cur, checker) > 0;
    }

    private static boolean diagonalStepBlocked(WalkabilityChecker checker,
                                               int fromX, int fromZ,
                                               int toX, int toY, int toZ) {
        if (fromX == toX || fromZ == toZ) {
            return false;
        }

        return sideCellBlocked(checker, toX, toY, fromZ)
                || sideCellBlocked(checker, fromX, toY, toZ);
    }

    private static boolean sideCellBlocked(WalkabilityChecker checker, int x, int y, int z) {
        return !checker.isPassable(x, y, z)
                || !checker.isPassable(x, y + 1, z)
                || checker.isFullWall(x, y, z)
                || checker.isFullWall(x, y + 1, z);
    }

    @FunctionalInterface
    private interface CellClearance {
        boolean clear(int x, int y, int z);
    }

    private record GridLine(
        int x0,
        int y0,
        int z0,
        int x1,
        int y1,
        int z1,
        int dx,
        int dy,
        int dz,
        int sx,
        int sy,
        int sz
    ) {
        private static GridLine between(Node from, Node to) {
            int fromX = from.position.flooredX();
            int fromY = from.position.flooredY();
            int fromZ = from.position.flooredZ();
            int toX = to.position.flooredX();
            int toY = to.position.flooredY();
            int toZ = to.position.flooredZ();
            return new GridLine(
                fromX,
                fromY,
                fromZ,
                toX,
                toY,
                toZ,
                Math.abs(toX - fromX),
                Math.abs(toY - fromY),
                Math.abs(toZ - fromZ),
                stepSign(fromX, toX),
                stepSign(fromY, toY),
                stepSign(fromZ, toZ));
        }

        private static int stepSign(int from, int to) {
            return from < to ? 1 : -1;
        }
    }
}
