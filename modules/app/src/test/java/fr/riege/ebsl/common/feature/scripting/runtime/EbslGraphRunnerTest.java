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

import fr.riege.ebsl.common.core.event.*;
import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnectionMode;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNode;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPort;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.layer.*;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EbslGraphRunnerTest {
    @Test
    void executesGraphNodesWithoutCompilingTextSource() {
        RecordingCommands commands = new RecordingCommands();
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection("first-second", "first", "main", "second", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(
                "first", EbslGraphNode.action("first", "message", Map.of("text", "hello")),
                "second", EbslGraphNode.action("second", "message", Map.of("text", "world"))));

        EbslGraphRunner runner = EbslScriptEngine.graphRunner(document, platform(commands));
        runner.start();
        runner.tick();

        assertTrue(runner.done());
        assertEquals(List.of("hello", "world"), commands.messages);
    }

    @Test
    void dispatchesOnlyTheSelectedSwitchOutputPort() {
        RecordingCommands commands = new RecordingCommands();
        EbslGraphNode switchNode = EbslGraphNode.switchNode(
            "switch",
            Map.of("output", "right"),
            List.of(EbslGraphPort.output("left", "Left"), EbslGraphPort.output("right", "Right")));
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("left-edge", "switch", "left", "left", "main", EbslGraphConnectionMode.FLOW, ""),
                new EbslGraphConnection("right-edge", "switch", "right", "right", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(
                switchNode.id(), switchNode,
                "left", EbslGraphNode.action("left", "message", Map.of("text", "left")),
                "right", EbslGraphNode.action("right", "message", Map.of("text", "right"))));

        EbslGraphRunner runner = EbslScriptEngine.graphRunner(document, platform(commands));
        runner.start();
        runner.tick();

        assertEquals(List.of("right"), commands.messages);
    }

    @Test
    void dispatchesFanOutForRegularActionNodes() {
        RecordingCommands commands = new RecordingCommands();
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("root-left", "root", "main", "left", "main", EbslGraphConnectionMode.FLOW, ""),
                new EbslGraphConnection("root-right", "root", "main", "right", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(
                "root", EbslGraphNode.action("root", "message", Map.of("text", "root")),
                "left", EbslGraphNode.action("left", "message", Map.of("text", "left")),
                "right", EbslGraphNode.action("right", "message", Map.of("text", "right"))));

        EbslGraphRunner runner = EbslScriptEngine.graphRunner(document, platform(commands));
        runner.tick();

        assertEquals(List.of("root", "left", "right"), commands.messages);
    }

    @Test
    void keepsTimedNodesRunningAcrossTicks() {
        RecordingCommands commands = new RecordingCommands();
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection("wait-message", "wait", "main", "message", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(
                "wait", EbslGraphNode.action("wait", "wait", Map.of("duration", "2t")),
                "message", EbslGraphNode.action("message", "message", Map.of("text", "after"))));

        EbslGraphRunner runner = EbslScriptEngine.graphRunner(document, platform(commands));
        runner.tick();
        runner.tick();

        assertEquals(List.of(), commands.messages);

        runner.tick();

        assertTrue(runner.done());
        assertEquals(List.of("after"), commands.messages);
    }

    @Test
    void rejectsInvalidGraphBeforeRuntimeExecution() {
        RecordingCommands commands = new RecordingCommands();
        EbslGraphNode first = EbslGraphNode.action("first", "message", Map.of("text", "first"));
        EbslGraphNode second = EbslGraphNode.action("second", "message", Map.of("text", "second"));
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(
                new EbslGraphConnection("a", "first", "main", "second", "main", EbslGraphConnectionMode.FLOW, ""),
                new EbslGraphConnection("b", "second", "main", "first", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(first.id(), first, second.id(), second));

        EbslGraphRunner runner = EbslScriptEngine.graphRunner(document, platform(commands));
        runner.start();

        assertTrue(runner.done());
        assertEquals("invalid graph", runner.status());
        assertEquals(List.of(), commands.messages);
    }

    @Test
    void stopClearsQueuedGraphExecution() {
        RecordingCommands commands = new RecordingCommands();
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection("wait-message", "wait", "main", "message", "main", EbslGraphConnectionMode.FLOW, "")),
            Map.of(
                "wait", EbslGraphNode.action("wait", "wait", Map.of("duration", "10t")),
                "message", EbslGraphNode.action("message", "message", Map.of("text", "after"))));

        EbslGraphRunner runner = EbslScriptEngine.graphRunner(document, platform(commands));
        runner.tick();
        runner.stop();
        runner.tick();

        assertTrue(runner.done());
        assertEquals("stopped", runner.status());
        assertEquals(List.of(), commands.messages);
    }

    private static EbslPlatform platform(RecordingCommands commands) {
        return EbslPlatform.builder()
            .world(new EmptyWorld())
            .player(new AlivePlayer())
            .physics(new IPhysicsLayer() {})
            .events(new NoopEvents())
            .render(new NoopRender())
            .commands(commands)
            .storage(new MemoryStorage())
            .imgui(new NoopImGui())
            .input(new IInputLayer() {})
            .build();
    }

    private static final class RecordingCommands implements ICommandLayer {
        private final List<String> messages = new ArrayList<>();

        @Override public void register(String name, String description, CommandHandler handler) {
            // Test command layer does not expose command registration.
        }

        @Override public void print(String message) {
            messages.add(message);
        }
    }

    private static final class EmptyWorld implements IWorldLayer {
        @Override public BlockId getBlock(int x, int y, int z) { return BlockId.of("minecraft:air"); }
        @Override public boolean isAir(int x, int y, int z) { return true; }
        @Override public boolean isSolid(int x, int y, int z) { return false; }
        @Override public boolean isWater(int x, int y, int z) { return false; }
        @Override public boolean isLava(int x, int y, int z) { return false; }
        @Override public boolean isLoaded(int x, int y, int z) { return true; }
        @Override public int getTopSolidY(int x, int z) { return 0; }
        @Override public double getBlockHeight(int x, int y, int z) { return 0.0; }
    }

    private static final class AlivePlayer implements IPlayerLayer {
        @Override public Vec3d position() { return new Vec3d(0.0, 64.0, 0.0); }
        @Override public boolean isInWater() { return false; }
        @Override public boolean isInLava() { return false; }
        @Override public boolean isSprinting() { return false; }
        @Override public boolean isAlive() { return true; }
        @Override public float getHealth() { return 20.0f; }
    }

    private static final class NoopEvents implements IEventBus {
        @Override public void onTick(Consumer<TickEvent> handler) {
            // Test event bus keeps callbacks inert.
        }
        @Override public void onRenderWorld(Consumer<RenderWorldEvent> handler) {
            // Test event bus keeps callbacks inert.
        }
        @Override public void onRenderHud(Consumer<RenderHudEvent> handler) {
            // Test event bus keeps callbacks inert.
        }
        @Override public void onKeyPress(Consumer<KeyPressEvent> handler) {
            // Test event bus keeps callbacks inert.
        }
        @Override public void onMouseButton(Consumer<MouseButtonEvent> handler) {
            // Test event bus keeps callbacks inert.
        }
        @Override public void onCharTyped(Consumer<CharTypedEvent> handler) {
            // Test event bus keeps callbacks inert.
        }
    }

    private static final class NoopRender implements IRenderLayer {
        @Override public void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix) {
            // Graph runner tests do not render frames.
        }
        @Override public void endFrame() {
            // Graph runner tests do not render frames.
        }
        @Override public void beginLines(float r, float g, float b, float a) {
            // Graph runner tests do not render primitives.
        }
        @Override public void emitLine(double x1, double y1, double z1, double x2, double y2, double z2, float lineWidth) {
            // Graph runner tests do not render primitives.
        }
        @Override public void beginTriangles(float r, float g, float b, float a) {
            // Graph runner tests do not render primitives.
        }
        @Override public void emitTriangle(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
            // Graph runner tests do not render primitives.
        }
        @Override public void end(boolean ignoreDepth) {
            // Graph runner tests do not render primitives.
        }
        @Override public double cameraX() { return 0.0; }
        @Override public double cameraY() { return 0.0; }
        @Override public double cameraZ() { return 0.0; }
    }

    private static final class NoopImGui implements IImGuiLayer {
        @Override public void registerFrame(Runnable drawPanels) {
            // Graph runner tests do not draw ImGui panels.
        }
        @Override public int getViewportWidth() { return 1920; }
        @Override public int getViewportHeight() { return 1080; }
    }

    private static final class MemoryStorage implements IStorageLayer {
        @Override public void saveJson(String key, String json) {
            // Graph runner tests do not persist state.
        }
        @Override public Optional<String> loadJson(String key) { return Optional.empty(); }
        @Override public void saveText(String path, String text) {
            // Graph runner tests do not persist state.
        }
        @Override public Optional<String> loadText(String path) { return Optional.empty(); }
    }
}
