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
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.scenario.SimulationScenario;
import fr.riege.ebsl.tools.pathfindersim.core.SimulationSuite;
import fr.riege.ebsl.tools.pathfindersim.cli.SimCliOptions;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldImportOptions;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldScenarioFactory;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public final class SimulationFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_TIMER_DELAY_MS = 50;
    private static final int MIN_TIMER_DELAY_MS = 10;
    private static final int MAX_TIMER_DELAY_MS = 250;

    private final transient SimCliOptions options;
    private final transient MinecraftWorldImportOptions minecraftOptions;
    private final transient List<SimulationResult> results = new ArrayList<>();
    private final ReplayCanvas canvas = new ReplayCanvas();
    private final MetricsCanvas metricsCanvas = new MetricsCanvas();
    private final JSlider timeline = new JSlider();
    private final JLabel status = new JLabel("ready");
    private final JLabel frameStatus = new JLabel("tick 0");
    private final JTextArea details = new JTextArea();
    private final JList<SimulationResult> scenarioList = new JList<>();
    private final DefaultListModel<SimulationResult> scenarioModel = new DefaultListModel<>();
    private final DefaultComboBoxModel<SimulationResult> scenarioComboModel = new DefaultComboBoxModel<>();
    private final JComboBox<SimulationResult> scenarioCombo = new JComboBox<>();
    private final JTextField worldField = new JTextField(22);
    private final JTextField startField = new JTextField(16);
    private final JTextField goalField = new JTextField(12);
    private final JComboBox<SimulationGoalInput> goalType = new JComboBox<>(SimulationGoalInput.values());
    private final JCheckBox isometric = new JCheckBox("3D", true);
    private final JCheckBox minecraftSpeed = new JCheckBox("20 TPS", true);
    private final Timer timer;
    private transient SimulationResult selected;
    private JButton playButton;

    private SimulationFrame(List<SimulationResult> results, SimCliOptions options) {
        super("EBSL Pathfinder Simulator");
        this.results.addAll(results);
        this.options = options;
        this.minecraftOptions = options == null ? null : options.minecraftWorldImportOptions();
        this.timer = new Timer(DEFAULT_TIMER_DELAY_MS, event -> stepForward());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setPreferredSize(new Dimension(1320, 820));
        setLayout(new BorderLayout());
        add(header(), BorderLayout.NORTH);
        add(tabbedView(), BorderLayout.CENTER);
        add(timelinePanel(), BorderLayout.SOUTH);
        selectResult(this.results.isEmpty() ? null : this.results.getFirst());
        pack();
        setLocationRelativeTo(null);
    }

    public static void show(List<SimulationResult> results) {
        show(results, null);
    }

    public static void show(List<SimulationResult> results, SimCliOptions options) {
        SwingUtilities.invokeLater(() -> new SimulationFrame(results, options).setVisible(true));
    }

    private JPanel header() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JPanel replayRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JPanel routeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        scenarioCombo.setModel(scenarioComboModel);
        scenarioCombo.setRenderer(new SimulationResultRenderer());
        scenarioCombo.addActionListener(event -> selectResult((SimulationResult) scenarioCombo.getSelectedItem()));
        playButton = new JButton("Play");
        playButton.addActionListener(event -> togglePlayback());
        JButton reset = new JButton("Reset");
        reset.addActionListener(event -> resetPlayback());
        JButton previousStuck = new JButton("Prev stuck");
        previousStuck.addActionListener(event -> jumpToStuck(-1));
        JButton nextStuck = new JButton("Next stuck");
        nextStuck.addActionListener(event -> jumpToStuck(1));
        JButton resetView = new JButton("View");
        resetView.addActionListener(event -> canvas.resetCamera());
        JButton browseWorld = new JButton("Browse");
        browseWorld.addActionListener(event -> browseWorld());
        JButton runRoute = new JButton("Run route");
        runRoute.addActionListener(event -> runRoute());
        goalType.addActionListener(event -> updateGoalHint());
        JSlider speed = new JSlider(1, 10, 5);
        speed.setPreferredSize(new Dimension(120, 28));
        speed.addChangeListener(event -> updateSpeed(speed.getValue()));
        speed.setEnabled(false);
        minecraftSpeed.addActionListener(event -> updateSpeedMode(speed));
        isometric.addActionListener(event -> canvas.setIsometric(isometric.isSelected()));
        seedRouteFields();
        replayRow.add(new JLabel("Scenario"));
        replayRow.add(scenarioCombo);
        replayRow.add(playButton);
        replayRow.add(reset);
        replayRow.add(previousStuck);
        replayRow.add(nextStuck);
        replayRow.add(resetView);
        replayRow.add(isometric);
        replayRow.add(minecraftSpeed);
        replayRow.add(new JLabel("Speed"));
        replayRow.add(speed);
        replayRow.add(status);
        routeRow.add(new JLabel("World"));
        routeRow.add(worldField);
        routeRow.add(browseWorld);
        routeRow.add(new JLabel("Start"));
        routeRow.add(startField);
        routeRow.add(new JLabel("Goal"));
        routeRow.add(goalType);
        routeRow.add(goalField);
        routeRow.add(runRoute);
        panel.add(replayRow);
        panel.add(routeRow);
        return panel;
    }

    private JTabbedPane tabbedView() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Replay", splitView());
        tabs.addTab("Charts", metricsCanvas);
        tabs.addTab("Summary", summaryPanel());
        return tabs;
    }

    private JSplitPane splitView() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar(), canvas);
        split.setResizeWeight(0.22);
        split.setBorder(BorderFactory.createEmptyBorder());
        return split;
    }

    private JPanel summaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(details), BorderLayout.CENTER);
        return panel;
    }

    private JPanel sidebar() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        refreshScenarioList();
        scenarioList.setModel(scenarioModel);
        scenarioList.setCellRenderer(new SimulationResultRenderer());
        scenarioList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scenarioList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                selectResult(scenarioList.getSelectedValue());
            }
        });
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        panel.add(new JScrollPane(scenarioList), BorderLayout.CENTER);
        return panel;
    }

    private void togglePlayback() {
        if (timer.isRunning()) {
            timer.stop();
            playButton.setText("Play");
        } else {
            timer.start();
            playButton.setText("Pause");
        }
    }

    private void resetPlayback() {
        timer.stop();
        if (playButton != null) {
            playButton.setText("Play");
        }
        timeline.setValue(0);
        updateFrame();
    }

    private void jumpToStuck(int direction) {
        if (selected == null || selected.ticksTrace().isEmpty()) {
            return;
        }
        int current = timeline.getValue();
        int next = current;
        for (int i = current + direction; i >= 0 && i < selected.ticksTrace().size(); i += direction) {
            if (selected.ticksTrace().get(i).stuck()) {
                next = i;
                break;
            }
        }
        timeline.setValue(next);
    }

    private void updateSpeed(int value) {
        if (minecraftSpeed.isSelected()) {
            timer.setDelay(DEFAULT_TIMER_DELAY_MS);
            return;
        }
        int delay = Math.clamp(
            MAX_TIMER_DELAY_MS - value * 24L,
            MIN_TIMER_DELAY_MS,
            MAX_TIMER_DELAY_MS);
        timer.setDelay(delay);
    }

    private void updateSpeedMode(JSlider speed) {
        speed.setEnabled(!minecraftSpeed.isSelected());
        updateSpeed(speed.getValue());
    }

    private void seedRouteFields() {
        if (minecraftOptions == null) {
            worldField.setText("");
            startField.setText(formatVec(0.5, 64.0, 0.5));
            goalField.setText(formatVec(0, 64, 0));
            updateGoalHint();
            return;
        }
        worldField.setText(minecraftOptions.worldDirectory().toString());
        startField.setText(formatVec(minecraftOptions.start().x(), minecraftOptions.start().y(), minecraftOptions.start().z()));
        goalField.setText(formatVec(minecraftOptions.goalX(), minecraftOptions.goalY(), minecraftOptions.goalZ()));
        updateGoalHint();
    }

    private void browseWorld() {
        JFileChooser chooser = new JFileChooser(initialBrowseDirectory().toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Minecraft world folder");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            worldField.setText(chooser.getSelectedFile().toPath().toString());
        }
    }

    private void runRoute() {
        if (worldField.getText().isBlank()) {
            status.setText("choose a Minecraft world folder first");
            return;
        }
        status.setText("running route...");
        new SwingWorker<List<SimulationResult>, Void>() {
            @Override
            protected List<SimulationResult> doInBackground() throws IOException {
                MinecraftWorldImportOptions routeOptions = routeOptions();
                List<SimulationScenario> scenarios = MinecraftWorldScenarioFactory.create(routeOptions, null);
                return new SimulationSuite(scenarios).run(headlessOptions(routeOptions));
            }

            @Override
            protected void done() {
                try {
                    replaceResults(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    status.setText("route interrupted");
                } catch (ExecutionException exception) {
                    status.setText("route failed: " + rootMessage(exception));
                }
            }
        }.execute();
    }

    private MinecraftWorldImportOptions routeOptions() {
        double[] start = parseStartField();
        SimulationGoalInput selectedGoalType = (SimulationGoalInput) goalType.getSelectedItem();
        if (selectedGoalType == null) {
            selectedGoalType = SimulationGoalInput.fallback();
        }
        return selectedGoalType.toOptions(
            goalField.getText(),
            new Vec3d(start[0], start[1], start[2]),
            Path.of(worldField.getText().trim()),
            routeBaseOptions());
    }

    private SimCliOptions headlessOptions(MinecraftWorldImportOptions routeOptions) {
        SimCliOptions source = options == null ? SimCliOptions.parse(new String[] { "--headless" }) : options;
        return new SimCliOptions(
            "all",
            source.maxTicks(),
            source.stuckWindowTicks(),
            source.stuckEpsilon(),
            null,
            true,
            routeOptions,
            null);
    }

    private double[] parseStartField() {
        String[] parts = startField.getText().split(",");
        if (parts.length != 3) {
            return defaultStart();
        }
        try {
            return new double[] {
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
            };
        } catch (NumberFormatException ignored) {
            return defaultStart();
        }
    }

    private void replaceResults(List<SimulationResult> replacement) {
        results.clear();
        results.addAll(replacement);
        refreshScenarioList();
        selectResult(results.isEmpty() ? null : results.getFirst());
    }

    private void refreshScenarioList() {
        scenarioModel.clear();
        scenarioComboModel.removeAllElements();
        for (SimulationResult result : results) {
            scenarioModel.addElement(result);
            scenarioComboModel.addElement(result);
        }
    }

    private void selectResult(SimulationResult result) {
        if (result == null) {
            selected = null;
            timeline.setMinimum(0);
            timeline.setMaximum(0);
            timeline.setValue(0);
            canvas.setResult(null);
            metricsCanvas.setResult(null);
            updateFrame();
            return;
        }
        selected = result;
        if (scenarioList.getSelectedValue() != result) {
            scenarioList.setSelectedValue(result, true);
        }
        if (scenarioCombo.getSelectedItem() != result) {
            scenarioCombo.setSelectedItem(result);
        }
        int max = Math.max(0, result.ticksTrace().size() - 1);
        timeline.setMinimum(0);
        timeline.setMaximum(max);
        timeline.setValue(0);
        canvas.setResult(result);
        metricsCanvas.setResult(result);
        details.setText(detailsText(result));
        updateFrame();
    }

    private String detailsText(SimulationResult result) {
        return """
            %s

            status: %s
            reached: %s
            ticks: %d
            nodes: %d / raw %d
            stuck: %d ticks, %d events
            final distance: %.4f

            %s
            """.formatted(
            result.scenarioId(),
            result.status(),
            result.reached(),
            result.ticks(),
            result.navigationNodes(),
            result.rawNodes(),
            result.metrics().stuckTicks(),
            result.metrics().stuckEvents(),
            result.metrics().finalDistance(),
            result.description());
    }

    private JPanel timelinePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        timeline.addChangeListener(event -> updateFrame());
        panel.add(frameStatus, BorderLayout.WEST);
        panel.add(timeline, BorderLayout.CENTER);
        return panel;
    }

    private void stepForward() {
        if (timeline.getValue() >= timeline.getMaximum()) {
            timer.stop();
            if (playButton != null) {
                playButton.setText("Play");
            }
            return;
        }
        timeline.setValue(timeline.getValue() + 1);
    }

    private void updateFrame() {
        canvas.setFrame(timeline.getValue());
        if (selected == null) {
            status.setText("no replay");
            frameStatus.setText("tick 0");
            details.setText("");
            return;
        }
        int index = Math.clamp(timeline.getValue(), 0, Math.max(0, selected.ticksTrace().size() - 1));
        frameStatus.setText("tick " + index + " / " + Math.max(0, selected.ticksTrace().size() - 1));
        if (selected.ticksTrace().isEmpty()) {
            status.setText("empty trace");
            return;
        }
        var tick = selected.ticksTrace().get(index);
        status.setText(String.format(Locale.ROOT,
            "status %s | move %s | dist %.2f | stuck %s",
            selected.status(),
            tick.moveType(),
            tick.distanceToGoal(),
            tick.stuck()));
    }

    private static String formatVec(double x, double y, double z) {
        return String.format(Locale.ROOT, "%.1f,%.1f,%.1f", x, y, z);
    }

    private MinecraftWorldImportOptions routeBaseOptions() {
        if (minecraftOptions != null) {
            return minecraftOptions;
        }
        return new MinecraftWorldImportOptions(
            Path.of("."),
            new Vec3d(0.5, 64.0, 0.5),
            true,
            0,
            64,
            0,
            true,
            MinecraftWorldImportOptions.DEFAULT_RADIUS_CHUNKS,
            MinecraftWorldImportOptions.DEFAULT_GOAL_SEARCH_BLOCKS,
            false);
    }

    private double[] defaultStart() {
        Vec3d start = routeBaseOptions().start();
        return new double[] { start.x(), start.y(), start.z() };
    }

    private Path initialBrowseDirectory() {
        String current = worldField.getText().trim();
        if (!current.isBlank()) {
            return Path.of(current);
        }
        Path saves = Path.of("run", "saves").toAbsolutePath().normalize();
        if (Files.isDirectory(saves)) {
            return saves;
        }
        return Path.of(System.getProperty("user.dir"));
    }

    private void updateGoalHint() {
        SimulationGoalInput selectedGoalType = (SimulationGoalInput) goalType.getSelectedItem();
        if (selectedGoalType != null) {
            goalField.setToolTipText(selectedGoalType.hint());
        }
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage();
    }
}
