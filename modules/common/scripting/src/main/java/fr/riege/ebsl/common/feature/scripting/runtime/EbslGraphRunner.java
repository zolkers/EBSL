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

package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.feature.scripting.registry.ScriptingRegistries;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.EbslNodeField;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphExecutionPlan;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphExecutionPlanner;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNode;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EbslGraphRunner {
    private static final int MAX_NODE_STEPS_PER_TICK = 64;
    private static final String SWITCH_NODE_TYPE = "switch";

    private final EbslGraphDocument document;
    private final EbslGraphExecutionPlan plan;
    private final EbslScriptRuntime runtime;
    private final ArrayDeque<NodeExecution> queue = new ArrayDeque<>();
    private boolean started;
    private boolean done;
    private String status = "idle";

    EbslGraphRunner(EbslGraphDocument document, EbslPlatform platform) {
        this(document, platform, null);
    }

    EbslGraphRunner(EbslGraphDocument document, EbslPlatform platform, NavigationService navigation) {
        this.document = document;
        this.plan = EbslGraphExecutionPlanner.plan(document);
        this.runtime = new EbslScriptRuntime(platform, navigation);
    }

    public void start() {
        queue.clear();
        if (!plan.executable()) {
            done = true;
            status = "invalid graph";
            return;
        }
        for (String root : plan.roots()) {
            enqueue(root);
        }
        started = true;
        done = queue.isEmpty();
        status = done ? "done" : "running";
    }

    public void tick() {
        if (!started) {
            start();
        }
        if (done) {
            return;
        }
        int budget = MAX_NODE_STEPS_PER_TICK;
        while (budget-- > 0 && !queue.isEmpty() && !runtime.stopped()) {
            NodeExecution execution = queue.peek();
            if (execution.tick(runtime) == EbslStep.RUNNING) {
                return;
            }
            queue.removeFirst();
            dispatch(execution.graphNode());
        }
        if (runtime.stopped()) {
            done = true;
            status = "stopped";
            return;
        }
        if (queue.isEmpty()) {
            done = true;
            status = "done";
        }
    }

    public void stop() {
        if (runtime.platform() != null) {
            runtime.platform().input().releaseGameplayKeys();
        }
        queue.clear();
        done = true;
        status = "stopped";
    }

    public boolean done() {
        return done;
    }

    public String status() {
        return status;
    }

    public EbslGraphExecutionPlan plan() {
        return plan;
    }

    private void dispatch(EbslGraphNode graphNode) {
        List<String> selectedPorts = selectedOutputPorts(graphNode);
        for (String port : selectedPorts) {
            for (EbslGraphConnection connection : plan.next(graphNode.id(), port)) {
                enqueue(connection.toKey());
            }
        }
    }

    private List<String> selectedOutputPorts(EbslGraphNode graphNode) {
        String configuredOutput = graphNode.fields().getOrDefault("output", "");
        if (SWITCH_NODE_TYPE.equals(graphNode.type()) && !configuredOutput.isBlank()) {
            return List.of(configuredOutput);
        }
        return plan.next(graphNode.id()).stream()
            .map(EbslGraphConnection::fromPort)
            .distinct()
            .toList();
    }

    private void enqueue(String nodeId) {
        EbslGraphNode graphNode = document.nodes().get(nodeId);
        if (graphNode != null) {
            queue.addLast(new NodeExecution(graphNode));
        }
    }

    private static final class NodeExecution {
        private final EbslGraphNode graphNode;
        private final EbslNode node;
        private final List<String> args;
        private boolean started;
        private int ticksLeft;

        private NodeExecution(EbslGraphNode graphNode) {
            this.graphNode = graphNode;
            this.node = ScriptingRegistries.scripting().nodes().create(graphNode.type());
            this.args = node == null ? List.of() : invocationArgs(node, graphNode.fields());
        }

        private EbslGraphNode graphNode() {
            return graphNode;
        }

        private EbslStep tick(EbslScriptRuntime runtime) {
            if (node == null) {
                return EbslStep.DONE;
            }
            EbslNodeInvocation invocation = new EbslNodeInvocation(args, runtime, null);
            if (!started) {
                started = true;
                node.loadArgs(args);
                ticksLeft = node.start(invocation);
            }
            if (ticksLeft > 0) {
                node.tick(invocation);
                if (!node.isComplete(invocation)) {
                    ticksLeft--;
                    return EbslStep.RUNNING;
                }
            }
            if (node.waitsForNavigation() && runtime.navigation().isNavigating()) {
                return EbslStep.RUNNING;
            }
            if (node.releasesGameplayKeys()) {
                runtime.platform().input().releaseGameplayKeys();
            }
            node.finish(invocation);
            return EbslStep.DONE;
        }

        private static List<String> invocationArgs(EbslNode node, Map<String, String> fields) {
            return node.fields().stream()
                .sorted(Comparator.comparingInt(EbslNodeField::argumentIndex))
                .map(field -> fields.getOrDefault(field.id(), field.defaultValue()))
                .toList();
        }
    }
}
