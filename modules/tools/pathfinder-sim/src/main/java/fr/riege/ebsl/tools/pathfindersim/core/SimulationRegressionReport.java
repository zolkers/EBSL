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

package fr.riege.ebsl.tools.pathfindersim.core;

import fr.riege.ebsl.tools.pathfindersim.cli.SimCliOptions;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record SimulationRegressionReport(List<SimulationRegressionIssue> issues) {
    private static final String EXCEEDED = " exceeded ";

    public SimulationRegressionReport {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public static SimulationRegressionReport evaluate(List<SimulationResult> results, SimCliOptions options) {
        List<SimulationResult> simulations = results == null ? List.of() : results;
        SimCliOptions effectiveOptions = options == null ? SimCliOptions.parse(new String[0]) : options;
        List<SimulationRegressionIssue> issues = new ArrayList<>();
        if (simulations.isEmpty()) {
            issues.add(new SimulationRegressionIssue("suite", "no simulation result was produced"));
        }
        for (SimulationResult result : simulations) {
            evaluateResult(result, effectiveOptions, issues);
        }
        return new SimulationRegressionReport(issues);
    }

    public boolean passed() {
        return issues.isEmpty();
    }

    public String render() {
        if (passed()) {
            return "Simulation regression passed.";
        }
        StringBuilder builder = new StringBuilder("Simulation regression failed:");
        for (SimulationRegressionIssue issue : issues) {
            builder.append(System.lineSeparator())
                .append("- ")
                .append(issue.scenarioId())
                .append(": ")
                .append(issue.reason());
        }
        return builder.toString();
    }

    private static void evaluateResult(SimulationResult result,
                                       SimCliOptions options,
                                       List<SimulationRegressionIssue> issues) {
        if (!result.reached()) {
            issues.add(new SimulationRegressionIssue(result.scenarioId(), "goal was not reached"));
        }
        double finalDistance = result.metrics().finalDistance();
        if (finalDistance > options.regressionMaxFinalDistance()) {
            issues.add(new SimulationRegressionIssue(
                result.scenarioId(),
                String.format(
                    Locale.ROOT,
                    "final distance %.3f exceeded %.3f",
                    finalDistance,
                    options.regressionMaxFinalDistance())));
        }
        int stuckEvents = result.metrics().stuckEvents();
        if (stuckEvents > options.regressionMaxStuckEvents()) {
            issues.add(new SimulationRegressionIssue(
                result.scenarioId(),
                "stuck events " + stuckEvents + EXCEEDED + options.regressionMaxStuckEvents()));
        }
        int recoveryAttempts = result.metrics().recoveryAttempts();
        if (recoveryAttempts > options.regressionMaxRecoveryAttempts()) {
            issues.add(new SimulationRegressionIssue(
                result.scenarioId(),
                "recovery attempts " + recoveryAttempts + EXCEEDED + options.regressionMaxRecoveryAttempts()));
        }
        int backwardTicks = result.metrics().backwardTicks();
        if (backwardTicks > options.regressionMaxBackwardTicks()) {
            issues.add(new SimulationRegressionIssue(
                result.scenarioId(),
                "backward ticks " + backwardTicks + EXCEEDED + options.regressionMaxBackwardTicks()));
        }
        double averageLateralError = result.metrics().averageLateralError();
        if (averageLateralError > options.regressionMaxAverageLateralError()) {
            issues.add(new SimulationRegressionIssue(
                result.scenarioId(),
                String.format(
                    Locale.ROOT,
                    "average lateral error %.3f exceeded %.3f",
                    averageLateralError,
                    options.regressionMaxAverageLateralError())));
        }
    }

    public record SimulationRegressionIssue(String scenarioId, String reason) {
    }
}
