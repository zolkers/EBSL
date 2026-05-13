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
package fr.riege.ebsl.common.feature.ui.imgui.panel;

import java.util.*;

@SuppressWarnings({"java:S107", "java:S3776"})
final class GraphEdgeRouter {
    private GraphEdgeRouter() {
    }

    static List<EdgePoint> route(EdgePoint start, EdgePoint end, List<NodeBounds> nodes, float margin) {
        List<EdgeObstacle> obstacles = edgeObstacles(nodes, margin);
        List<Float> xs = new ArrayList<>();
        List<Float> ys = new ArrayList<>();
        addCoord(xs, start.x());
        addCoord(xs, end.x());
        addCoord(ys, start.y());
        addCoord(ys, end.y());
        float minX = Math.min(start.x(), end.x());
        float maxX = Math.max(start.x(), end.x());
        float minY = Math.min(start.y(), end.y());
        float maxY = Math.max(start.y(), end.y());
        for (EdgeObstacle obstacle : obstacles) {
            minX = Math.min(minX, obstacle.left());
            maxX = Math.max(maxX, obstacle.right());
            minY = Math.min(minY, obstacle.top());
            maxY = Math.max(maxY, obstacle.bottom());
            addCoord(xs, obstacle.left());
            addCoord(xs, obstacle.right());
            addCoord(ys, obstacle.top());
            addCoord(ys, obstacle.bottom());
        }
        addCoord(xs, minX - margin * 1.5f);
        addCoord(xs, maxX + margin * 1.5f);
        addCoord(ys, minY - margin * 1.5f);
        addCoord(ys, maxY + margin * 1.5f);
        Collections.sort(xs);
        Collections.sort(ys);

        int sx = coordIndex(xs, start.x());
        int sy = coordIndex(ys, start.y());
        int ex = coordIndex(xs, end.x());
        int ey = coordIndex(ys, end.y());
        double[][] dist = new double[xs.size()][ys.size()];
        int[][] prevX = new int[xs.size()][ys.size()];
        int[][] prevY = new int[xs.size()][ys.size()];
        for (int x = 0; x < xs.size(); x++) {
            Arrays.fill(dist[x], Double.POSITIVE_INFINITY);
            Arrays.fill(prevX[x], -1);
            Arrays.fill(prevY[x], -1);
        }
        PriorityQueue<RouteNode> queue = new PriorityQueue<>(Comparator.comparingDouble(RouteNode::cost));
        dist[sx][sy] = 0.0;
        queue.add(new RouteNode(sx, sy, 0.0));
        boolean foundTarget = false;
        while (!queue.isEmpty() && !foundTarget) {
            RouteNode current = queue.poll();
            if (current.cost() <= dist[current.x()][current.y()]) {
                if (current.x() == ex && current.y() == ey) {
                    foundTarget = true;
                } else {
                    relaxRouteNeighbor(current.x(), current.y(), current.x() - 1, current.y(), xs, ys, obstacles, dist, prevX, prevY, queue);
                    relaxRouteNeighbor(current.x(), current.y(), current.x() + 1, current.y(), xs, ys, obstacles, dist, prevX, prevY, queue);
                    relaxRouteNeighbor(current.x(), current.y(), current.x(), current.y() - 1, xs, ys, obstacles, dist, prevX, prevY, queue);
                    relaxRouteNeighbor(current.x(), current.y(), current.x(), current.y() + 1, xs, ys, obstacles, dist, prevX, prevY, queue);
                }
            }
        }
        if (!Double.isFinite(dist[ex][ey])) {
            return List.of(start, end);
        }
        List<EdgePoint> reversed = new ArrayList<>();
        int x = ex;
        int y = ey;
        while (x >= 0 && y >= 0) {
            reversed.add(new EdgePoint(xs.get(x), ys.get(y)));
            if (x == sx && y == sy) {
                break;
            }
            int px = prevX[x][y];
            int py = prevY[x][y];
            x = px;
            y = py;
        }
        Collections.reverse(reversed);
        return simplifyRoute(reversed);
    }

    private static List<EdgeObstacle> edgeObstacles(List<NodeBounds> nodes, float margin) {
        List<EdgeObstacle> obstacles = new ArrayList<>();
        for (NodeBounds node : nodes) {
            obstacles.add(new EdgeObstacle(node.x() - margin, node.y() - margin, node.right() + margin, node.bottom() + margin));
        }
        return obstacles;
    }

    private static void relaxRouteNeighbor(int x, int y, int nx, int ny, List<Float> xs, List<Float> ys,
                                           List<EdgeObstacle> obstacles, double[][] dist, int[][] prevX,
                                           int[][] prevY, PriorityQueue<RouteNode> queue) {
        if (nx < 0 || ny < 0 || nx >= xs.size() || ny >= ys.size()) {
            return;
        }
        EdgePoint from = new EdgePoint(xs.get(x), ys.get(y));
        EdgePoint to = new EdgePoint(xs.get(nx), ys.get(ny));
        if (pointInsideObstacle(to, obstacles) || segmentIntersectsObstacle(from, to, obstacles)) {
            return;
        }
        double nextCost = dist[x][y] + Math.abs(to.x() - from.x()) + Math.abs(to.y() - from.y());
        if (nextCost >= dist[nx][ny]) {
            return;
        }
        dist[nx][ny] = nextCost;
        prevX[nx][ny] = x;
        prevY[nx][ny] = y;
        queue.add(new RouteNode(nx, ny, nextCost));
    }

    private static boolean pointInsideObstacle(EdgePoint point, List<EdgeObstacle> obstacles) {
        for (EdgeObstacle obstacle : obstacles) {
            if (point.x() > obstacle.left() && point.x() < obstacle.right()
                && point.y() > obstacle.top() && point.y() < obstacle.bottom()) {
                return true;
            }
        }
        return false;
    }

    private static boolean segmentIntersectsObstacle(EdgePoint from, EdgePoint to, List<EdgeObstacle> obstacles) {
        for (EdgeObstacle obstacle : obstacles) {
            if (Math.abs(from.y() - to.y()) < 0.5f) {
                float y = from.y();
                float minX = Math.min(from.x(), to.x());
                float maxX = Math.max(from.x(), to.x());
                if (y > obstacle.top() && y < obstacle.bottom()
                    && maxX > obstacle.left() && minX < obstacle.right()) {
                    return true;
                }
            } else if (Math.abs(from.x() - to.x()) < 0.5f) {
                float x = from.x();
                float minY = Math.min(from.y(), to.y());
                float maxY = Math.max(from.y(), to.y());
                if (x > obstacle.left() && x < obstacle.right()
                    && maxY > obstacle.top() && minY < obstacle.bottom()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<EdgePoint> simplifyRoute(List<EdgePoint> points) {
        if (points.size() <= 2) {
            return points;
        }
        List<EdgePoint> simplified = new ArrayList<>();
        simplified.add(points.getFirst());
        for (int i = 1; i < points.size() - 1; i++) {
            EdgePoint previous = simplified.getLast();
            EdgePoint current = points.get(i);
            EdgePoint next = points.get(i + 1);
            boolean horizontal = Math.abs(previous.y() - current.y()) < 0.5f && Math.abs(current.y() - next.y()) < 0.5f;
            boolean vertical = Math.abs(previous.x() - current.x()) < 0.5f && Math.abs(current.x() - next.x()) < 0.5f;
            if (!horizontal && !vertical) {
                simplified.add(current);
            }
        }
        simplified.add(points.getLast());
        return simplified;
    }

    private static void addCoord(List<Float> coords, float value) {
        for (float coord : coords) {
            if (Math.abs(coord - value) < 0.5f) {
                return;
            }
        }
        coords.add(value);
    }

    private static int coordIndex(List<Float> coords, float value) {
        for (int i = 0; i < coords.size(); i++) {
            if (Math.abs(coords.get(i) - value) < 0.5f) {
                return i;
            }
        }
        return -1;
    }

    record NodeBounds(float x, float y, float width, float height) {
        private float right() {
            return x + width;
        }

        private float bottom() {
            return y + height;
        }
    }

    record EdgePoint(float x, float y) {
    }

    private record EdgeObstacle(float left, float top, float right, float bottom) {
    }

    private record RouteNode(int x, int y, double cost) {
    }
}
