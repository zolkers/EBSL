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

package fr.riege.ebsl.tools.pathfindersim.replay;

import fr.riege.ebsl.common.math.Vec3d;

import java.util.List;
import java.util.Locale;

public final class SimulationReport {
    private SimulationReport() {
    }

    public static String render(List<SimulationResult> results) {
        StringBuilder builder = new StringBuilder();
        builder.append("EBSL pathfinder simulation\n");
        builder.append("==========================\n");
        for (SimulationResult result : results) {
            SimMetrics metrics = result.metrics();
            builder.append(result.reached() ? "[PASS] " : "[FAIL] ")
                .append(result.scenarioId())
                .append(" status=").append(result.status())
                .append(" ticks=").append(result.ticks())
                .append(" nodes=").append(result.navigationNodes())
                .append(" stuckTicks=").append(metrics.stuckTicks())
                .append(" stuckEvents=").append(metrics.stuckEvents())
                .append(" finalDistance=").append(format(metrics.finalDistance()))
                .append(" elapsedMs=").append(format(result.elapsedNanos() / 1_000_000.0))
                .append('\n');
            builder.append("  ").append(result.description()).append('\n');
        }
        return builder.toString();
    }

    public static String toJson(List<SimulationResult> results) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"results\":[");
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            appendResult(builder, results.get(i));
        }
        builder.append("]}");
        return builder.toString();
    }

    private static void appendResult(StringBuilder builder, SimulationResult result) {
        SimMetrics metrics = result.metrics();
        builder.append('{')
            .append("\"scenarioId\":\"").append(escape(result.scenarioId())).append("\",")
            .append("\"description\":\"").append(escape(result.description())).append("\",")
            .append("\"status\":\"").append(result.status()).append("\",")
            .append("\"reached\":").append(result.reached()).append(',')
            .append("\"ticks\":").append(result.ticks()).append(',')
            .append("\"elapsedNanos\":").append(result.elapsedNanos()).append(',')
            .append("\"navigationNodes\":").append(result.navigationNodes()).append(',')
            .append("\"rawNodes\":").append(result.rawNodes()).append(',')
            .append("\"completePlan\":").append(result.completePlan()).append(',')
            .append("\"metrics\":{")
            .append("\"stuckTicks\":").append(metrics.stuckTicks()).append(',')
            .append("\"stuckEvents\":").append(metrics.stuckEvents()).append(',')
            .append("\"longestStuckStreak\":").append(metrics.longestStuckStreak()).append(',')
            .append("\"bestDistance\":").append(format(metrics.bestDistance())).append(',')
            .append("\"finalDistance\":").append(format(metrics.finalDistance()))
            .append("},\"terrain\":[");
        appendTerrain(builder, result.terrain());
        builder.append("],\"trace\":[");
        appendTrace(builder, result.ticksTrace());
        builder.append("]}");
    }

    private static void appendTerrain(StringBuilder builder, List<ReplayBlock> terrain) {
        for (int i = 0; i < terrain.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            ReplayBlock block = terrain.get(i);
            builder.append('{')
                .append("\"x\":").append(block.x()).append(',')
                .append("\"y\":").append(block.y()).append(',')
                .append("\"z\":").append(block.z()).append(',')
                .append("\"kind\":\"").append(block.kind()).append("\"")
                .append('}');
        }
    }

    private static void appendTrace(StringBuilder builder, List<SimulationTick> ticks) {
        int stride = Math.max(1, ticks.size() / 120);
        for (int i = 0; i < ticks.size(); i += stride) {
            if (i > 0) {
                builder.append(',');
            }
            SimulationTick tick = ticks.get(i);
            builder.append('{')
                .append("\"tick\":").append(tick.tick()).append(',')
                .append("\"position\":").append(vec(tick.position())).append(',')
                .append("\"velocity\":").append(vec(tick.velocity())).append(',')
                .append("\"status\":\"").append(tick.status()).append("\",")
                .append("\"moveType\":\"").append(tick.moveType()).append("\",")
                .append("\"distanceToGoal\":").append(format(tick.distanceToGoal())).append(',')
                .append("\"stuck\":").append(tick.stuck()).append(',')
                .append("\"jump\":").append(tick.jump()).append(',')
                .append("\"sprint\":").append(tick.sprint()).append(',')
                .append("\"sneak\":").append(tick.sneak())
                .append('}');
        }
    }

    private static String vec(Vec3d value) {
        return '[' + format(value.x()) + ',' + format(value.y()) + ',' + format(value.z()) + ']';
    }

    private static String format(double value) {
        if (!Double.isFinite(value)) {
            return "0.0";
        }
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
