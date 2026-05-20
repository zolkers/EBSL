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

package fr.riege.ebsl.tools.pathfindersim.ui.render;

import fr.riege.ebsl.tools.pathfindersim.replay.ReplayBlock;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.ui.Bounds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReplayRenderModel {
    private final SimulationResult result;
    private final Bounds bounds;
    private final int terrainMinY;
    private final List<TerrainColumn> surfaceTerrain;
    private List<ReplayBlock> isoTerrain = List.of();

    private ReplayRenderModel(SimulationResult result) {
        this.result = result;
        this.bounds = result == null ? null : Bounds.of(result.ticksTrace(), result.terrain());
        this.terrainMinY = minTerrainY(result);
        this.surfaceTerrain = surfaceTerrain(result);
    }

    public static ReplayRenderModel of(SimulationResult result, double yawRadians) {
        ReplayRenderModel model = new ReplayRenderModel(result);
        model.updateYaw(yawRadians);
        return model;
    }

    public void updateYaw(double yawRadians) {
        if (result == null || bounds == null) {
            isoTerrain = List.of();
            return;
        }
        isoTerrain = result.terrain().stream()
            .sorted((left, right) -> Double.compare(
                ReplayProjection.isoDepth(bounds, yawRadians, left.x(), left.y(), left.z()),
                ReplayProjection.isoDepth(bounds, yawRadians, right.x(), right.y(), right.z())))
            .toList();
    }

    public SimulationResult result() {
        return result;
    }

    public Bounds bounds() {
        return bounds;
    }

    public int terrainMinY() {
        return terrainMinY;
    }

    public List<TerrainColumn> surfaceTerrain() {
        return surfaceTerrain;
    }

    public List<ReplayBlock> isoTerrain() {
        return isoTerrain;
    }

    private static List<TerrainColumn> surfaceTerrain(SimulationResult result) {
        if (result == null || result.terrain().isEmpty()) {
            return List.of();
        }
        Map<String, ReplayBlock> columns = new HashMap<>();
        for (ReplayBlock block : result.terrain()) {
            columns.merge(columnKey(block.x(), block.z()), block, ReplayRenderModel::highestBlock);
        }
        int fallback = terrainMinFallback(result.terrain());
        return columns.values().stream()
            .map(block -> terrainColumn(block, columns, fallback))
            .toList();
    }

    private static ReplayBlock highestBlock(ReplayBlock left, ReplayBlock right) {
        return left.y() >= right.y() ? left : right;
    }

    private static TerrainColumn terrainColumn(ReplayBlock block, Map<String, ReplayBlock> columns, int fallback) {
        return new TerrainColumn(block.x(), block.y(), block.z(), block.kind(), relief(block, columns, fallback));
    }

    private static int relief(ReplayBlock block, Map<String, ReplayBlock> columns, int fallback) {
        int west = surfaceY(columns, block.x() - 1, block.z(), fallback);
        int north = surfaceY(columns, block.x(), block.z() - 1, fallback);
        int westSlope = Math.toIntExact((long) block.y() - west);
        int northSlope = Math.toIntExact((long) block.y() - north);
        int combinedSlope = Math.toIntExact((long) westSlope + northSlope);
        return Math.clamp(combinedSlope, -3, 3);
    }

    private static int surfaceY(Map<String, ReplayBlock> columns, int x, int z, int fallback) {
        ReplayBlock block = columns.get(columnKey(x, z));
        return block == null ? fallback : block.y();
    }

    private static String columnKey(int x, int z) {
        return x + "," + z;
    }

    private static int terrainMinFallback(List<ReplayBlock> terrain) {
        return terrain.stream().mapToInt(ReplayBlock::y).min().orElse(0);
    }

    private static int minTerrainY(SimulationResult result) {
        if (result == null) {
            return 0;
        }
        return result.terrain().stream()
            .mapToInt(ReplayBlock::y)
            .min()
            .orElse(0);
    }
}
