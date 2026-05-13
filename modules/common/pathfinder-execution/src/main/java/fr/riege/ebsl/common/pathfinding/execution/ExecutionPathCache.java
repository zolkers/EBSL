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
package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.Node;

import java.util.Collections;
import java.util.List;

final class ExecutionPathCache {
    private static final ExecutionPathCache EMPTY = new ExecutionPathCache(Collections.emptyList());

    private final List<Node> path;
    private final double[] x;
    private final double[] y;
    private final double[] z;
    private final double[] segmentDx;
    private final double[] segmentDy;
    private final double[] segmentDz;
    private final double[] segmentLenSq;
    private final double[] segmentLen;
    private final double[] remainingFromNode;

    private ExecutionPathCache(List<Node> path) {
        this.path = path;
        int size = path.size();
        this.x = new double[size];
        this.y = new double[size];
        this.z = new double[size];
        int segments = Math.max(0, size - 1);
        this.segmentDx = new double[segments];
        this.segmentDy = new double[segments];
        this.segmentDz = new double[segments];
        this.segmentLenSq = new double[segments];
        this.segmentLen = new double[segments];
        this.remainingFromNode = new double[size];

        for (int i = 0; i < size; i++) {
            Node node = path.get(i);
            x[i] = node.position.centeredX();
            y[i] = node.position.flooredY();
            z[i] = node.position.centeredZ();
        }
        for (int i = 0; i < segments; i++) {
            segmentDx[i] = x[i + 1] - x[i];
            segmentDy[i] = y[i + 1] - y[i];
            segmentDz[i] = z[i + 1] - z[i];
            segmentLenSq[i] = segmentDx[i] * segmentDx[i] + segmentDy[i] * segmentDy[i] + segmentDz[i] * segmentDz[i];
            segmentLen[i] = Math.sqrt(segmentLenSq[i]);
        }
        for (int i = size - 2; i >= 0; i--) {
            remainingFromNode[i] = remainingFromNode[i + 1] + segmentLen[i];
        }
    }

    static ExecutionPathCache of(List<Node> path) {
        return path == null || path.isEmpty() ? EMPTY : new ExecutionPathCache(path);
    }

    boolean isEmpty() {
        return path.isEmpty();
    }

    int size() {
        return path.size();
    }

    double x(int index) {
        return x[index];
    }

    double y(int index) {
        return y[index];
    }

    double offPathY(int index) {
        return y[index] - PathTracker.OFF_PATH_VERTICAL_CURSOR_OFFSET;
    }

    double z(int index) {
        return z[index];
    }

    double segmentDx(int index) {
        return segmentDx[index];
    }

    double segmentDy(int index) {
        return segmentDy[index];
    }

    double segmentDz(int index) {
        return segmentDz[index];
    }

    double segmentLenSq(int index) {
        return segmentLenSq[index];
    }

    double segmentLen(int index) {
        return segmentLen[index];
    }

    double remainingFromNode(int index) {
        return remainingFromNode[index];
    }
}
