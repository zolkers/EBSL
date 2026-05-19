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

package fr.riege.ebsl.tools.pathfindersim.ui;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.goal.Goal;
import fr.riege.ebsl.common.pathfinding.goal.GoalAxisX;
import fr.riege.ebsl.common.pathfinding.goal.GoalAxisZ;
import fr.riege.ebsl.common.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.common.pathfinding.goal.GoalChunk;
import fr.riege.ebsl.common.pathfinding.goal.GoalColumn;
import fr.riege.ebsl.common.pathfinding.goal.GoalGetToBlock;
import fr.riege.ebsl.common.pathfinding.goal.GoalNear;
import fr.riege.ebsl.common.pathfinding.goal.GoalRectangleXZ;
import fr.riege.ebsl.common.pathfinding.goal.GoalXZ;
import fr.riege.ebsl.common.pathfinding.goal.GoalYLevel;
import fr.riege.ebsl.common.pathfinding.goal.NavigationTarget;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldImportOptions;

import java.nio.file.Path;

enum SimulationGoalInput {
    BLOCK("Block", "x,y,z"),
    NEAR("Near", "x,y,z,radius"),
    GET_TO_BLOCK("Get To", "x,y,z"),
    XZ("XZ", "x,z"),
    COLUMN("Column", "x,z,radius"),
    CHUNK("Chunk", "chunkX,chunkZ"),
    AXIS_X("Axis X", "x"),
    AXIS_Z("Axis Z", "z"),
    Y_LEVEL("Y Level", "y"),
    RECTANGLE("Rectangle", "minX,minZ,maxX,maxZ"),
    OFFSET("Offset", "dx,dy,dz");

    private final String label;
    private final String hint;

    SimulationGoalInput(String label, String hint) {
        this.label = label;
        this.hint = hint;
    }

    String hint() {
        return hint;
    }

    MinecraftWorldImportOptions toOptions(String input,
                                          Vec3d start,
                                          Path worldDirectory,
                                          MinecraftWorldImportOptions baseOptions) {
        Goal goal = toGoal(input, start);
        NavigationTarget target = goal.resolve(floor(start.x()), floor(start.y()), floor(start.z()));
        int[] block = switchTarget(target, floor(start.y()));
        return new MinecraftWorldImportOptions(
            worldDirectory,
            start,
            true,
            block[0],
            block[1],
            block[2],
            true,
            baseOptions.radiusChunks(),
            baseOptions.goalSearchBlocks(),
            baseOptions.diagnostics());
    }

    private Goal toGoal(String input, Vec3d start) {
        String[] parts = input.split(",");
        return switch (this) {
            case BLOCK -> new GoalBlock(integer(parts, 0), integer(parts, 1), integer(parts, 2));
            case NEAR -> new GoalNear(integer(parts, 0), integer(parts, 1), integer(parts, 2), decimal(parts, 3));
            case GET_TO_BLOCK -> new GoalGetToBlock(integer(parts, 0), integer(parts, 1), integer(parts, 2));
            case XZ -> new GoalXZ(integer(parts, 0), integer(parts, 1));
            case COLUMN -> new GoalColumn(integer(parts, 0), integer(parts, 1), decimal(parts, 2));
            case CHUNK -> new GoalChunk(integer(parts, 0), integer(parts, 1));
            case AXIS_X -> new GoalAxisX(integer(parts, 0));
            case AXIS_Z -> new GoalAxisZ(integer(parts, 0));
            case Y_LEVEL -> new GoalYLevel(integer(parts, 0));
            case RECTANGLE -> rectangle(parts);
            case OFFSET -> offset(parts, start);
        };
    }

    private static Goal rectangle(String[] parts) {
        int x1 = integer(parts, 0);
        int z1 = integer(parts, 1);
        int x2 = integer(parts, 2);
        int z2 = integer(parts, 3);
        return new GoalRectangleXZ(Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2));
    }

    private static Goal offset(String[] parts, Vec3d start) {
        int x = floor(start.x()) + integer(parts, 0);
        int y = floor(start.y()) + integer(parts, 1);
        int z = floor(start.z()) + integer(parts, 2);
        return new GoalBlock(x, y, z);
    }

    private static int[] switchTarget(NavigationTarget target, int fallbackY) {
        if (target instanceof NavigationTarget.Block block) {
            return new int[] { block.x(), block.y(), block.z() };
        }
        if (target instanceof NavigationTarget.Column column) {
            return new int[] { column.x(), fallbackY, column.z() };
        }
        throw new IllegalArgumentException("Unsupported navigation target: " + target);
    }

    private static int integer(String[] parts, int index) {
        try {
            return Integer.parseInt(parts[index].trim());
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            throw new IllegalArgumentException("Expected integer input at index " + index, exception);
        }
    }

    private static double decimal(String[] parts, int index) {
        try {
            return Double.parseDouble(parts[index].trim());
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            throw new IllegalArgumentException("Expected decimal input at index " + index, exception);
        }
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    @Override
    public String toString() {
        return label;
    }
}
