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

package fr.riege.ebsl.tools.pathfindersim.scenario;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessBlockState;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessWorldLayer;

import java.util.List;

public final class ScenarioCatalog {
    private static final int FLOOR_Y = 63;
    private static final int PLAYER_Y = 64;

    private ScenarioCatalog() {
    }

    public static List<SimulationScenario> defaultScenarios() {
        return List.of(
            flatSprint(),
            wallBypass(),
            parkourGap(),
            stairClimb(),
            ladderColumn());
    }

    private static SimulationScenario flatSprint() {
        return scenario(
            "flat_sprint_24",
            "straight flat sprint over 24 blocks",
            flatWorld(),
            new Vec3d(0.5, PLAYER_Y, 0.5),
            24,
            PLAYER_Y,
            0);
    }

    private static SimulationScenario wallBypass() {
        HeadlessWorldLayer world = flatWorld();
        for (int z = -4; z <= 4; z++) {
            if (z != 3) {
                world.set(8, PLAYER_Y, z, HeadlessBlockState.STONE);
                world.set(8, PLAYER_Y + 1, z, HeadlessBlockState.STONE);
            }
        }
        return scenario(
            "wall_bypass_gap",
            "flat route blocked by a two-high wall with one lateral gap",
            world,
            new Vec3d(0.5, PLAYER_Y, 0.5),
            16,
            PLAYER_Y,
            0);
    }

    private static SimulationScenario parkourGap() {
        HeadlessWorldLayer world = flatWorld();
        for (int x = 4; x <= 6; x++) {
            world.set(x, FLOOR_Y, 0, HeadlessBlockState.AIR);
        }
        PathPlannerOptions planner = basePlanner()
            .allowParkour(true)
            .allowJump(true)
            .allowFall(true)
            .build();
        return new SimulationScenario(
            "parkour_gap_3",
            "three-block gap between same-height platforms",
            world,
            new Vec3d(0.5, PLAYER_Y, 0.5),
            10,
            PLAYER_Y,
            0,
            planner,
            defaultFollower());
    }

    private static SimulationScenario stairClimb() {
        HeadlessWorldLayer world = flatWorld();
        world.fill(4, FLOOR_Y + 1, -1, 5, FLOOR_Y + 1, 1, HeadlessBlockState.STONE);
        world.fill(6, FLOOR_Y + 2, -1, 7, FLOOR_Y + 2, 1, HeadlessBlockState.STONE);
        return scenario(
            "stair_climb_two",
            "two gradual one-block ascents",
            world,
            new Vec3d(0.5, PLAYER_Y, 0.5),
            9,
            PLAYER_Y + 2,
            0);
    }

    private static SimulationScenario ladderColumn() {
        HeadlessWorldLayer world = flatWorld();
        HeadlessBlockState ladder = HeadlessBlockState.climbable(BlockId.of("minecraft:ladder"));
        world.fill(4, FLOOR_Y + 1, 0, 4, FLOOR_Y + 5, 0, ladder);
        world.set(4, FLOOR_Y + 5, 0, HeadlessBlockState.AIR);
        world.fill(3, FLOOR_Y + 4, -1, 5, FLOOR_Y + 4, 1, HeadlessBlockState.STONE);
        return scenario(
            "ladder_column",
            "vertical climb to a small upper platform",
            world,
            new Vec3d(4.5, PLAYER_Y, 0.5),
            4,
            PLAYER_Y + 5,
            0);
    }

    private static SimulationScenario scenario(String id, String description, HeadlessWorldLayer world,
                                               Vec3d start, int goalX, int goalY, int goalZ) {
        return new SimulationScenario(id, description, world, start, goalX, goalY, goalZ, basePlanner().build(), defaultFollower());
    }

    private static HeadlessWorldLayer flatWorld() {
        return new HeadlessWorldLayer()
            .heightRange(0, 128)
            .fill(-16, FLOOR_Y, -16, 48, FLOOR_Y, 16, HeadlessBlockState.STONE);
    }

    private static PathPlannerOptions.Builder basePlanner() {
        return PathPlannerOptions.defaults().toBuilder()
            .async(false)
            .fallback(true)
            .processPath(true)
            .maxIterations(80_000)
            .maxLength(10_000)
            .maxCalculationTimeMs(0);
    }

    private static EntityFollowerOptions defaultFollower() {
        return EntityFollowerOptions.defaults();
    }
}
