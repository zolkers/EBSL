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

import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;

public final class SimulationFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_TIMER_DELAY_MS = 50;
    private static final int MIN_TIMER_DELAY_MS = 10;
    private static final int MAX_TIMER_DELAY_MS = 250;

    private final transient List<SimulationResult> results;
    private final ReplayCanvas canvas = new ReplayCanvas();
    private final JSlider timeline = new JSlider();
    private final JLabel status = new JLabel("ready");
    private final JLabel frameStatus = new JLabel("tick 0");
    private final JTextArea details = new JTextArea();
    private final JList<SimulationResult> scenarioList = new JList<>();
    private final Timer timer;
    private transient SimulationResult selected;
    private JButton playButton;

    private SimulationFrame(List<SimulationResult> results) {
        super("EBSL Pathfinder Simulator");
        this.results = List.copyOf(results);
        this.timer = new Timer(DEFAULT_TIMER_DELAY_MS, event -> stepForward());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setLayout(new BorderLayout());
        add(header(), BorderLayout.NORTH);
        add(splitView(), BorderLayout.CENTER);
        add(timelinePanel(), BorderLayout.SOUTH);
        selectResult(this.results.isEmpty() ? null : this.results.getFirst());
        pack();
        setLocationRelativeTo(null);
    }

    public static void show(List<SimulationResult> results) {
        SwingUtilities.invokeLater(() -> new SimulationFrame(results).setVisible(true));
    }

    private JPanel header() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JComboBox<SimulationResult> scenarios = new JComboBox<>();
        scenarios.setModel(new DefaultComboBoxModel<>(results.toArray(SimulationResult[]::new)));
        scenarios.setRenderer(new SimulationResultRenderer());
        scenarios.addActionListener(event -> selectResult((SimulationResult) scenarios.getSelectedItem()));
        playButton = new JButton("Play");
        playButton.addActionListener(event -> togglePlayback());
        JButton reset = new JButton("Reset");
        reset.addActionListener(event -> resetPlayback());
        JButton previousStuck = new JButton("Prev stuck");
        previousStuck.addActionListener(event -> jumpToStuck(-1));
        JButton nextStuck = new JButton("Next stuck");
        nextStuck.addActionListener(event -> jumpToStuck(1));
        JSlider speed = new JSlider(1, 10, 5);
        speed.setPreferredSize(new Dimension(120, 28));
        speed.addChangeListener(event -> updateSpeed(speed.getValue()));
        panel.add(new JLabel("Scenario"));
        panel.add(scenarios);
        panel.add(playButton);
        panel.add(reset);
        panel.add(previousStuck);
        panel.add(nextStuck);
        panel.add(new JLabel("Speed"));
        panel.add(speed);
        panel.add(status);
        return panel;
    }

    private JSplitPane splitView() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar(), canvas);
        split.setResizeWeight(0.22);
        split.setBorder(BorderFactory.createEmptyBorder());
        return split;
    }

    private JPanel sidebar() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        DefaultListModel<SimulationResult> model = new DefaultListModel<>();
        results.forEach(model::addElement);
        scenarioList.setModel(model);
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
        JPanel metrics = new JPanel(new GridLayout(1, 1));
        metrics.add(new JScrollPane(details));
        panel.add(new JScrollPane(scenarioList), BorderLayout.CENTER);
        panel.add(metrics, BorderLayout.SOUTH);
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
        int delay = Math.max(MIN_TIMER_DELAY_MS, MAX_TIMER_DELAY_MS - value * 24);
        timer.setDelay(delay);
    }

    private void selectResult(SimulationResult result) {
        if (result == null) {
            selected = null;
            timeline.setMinimum(0);
            timeline.setMaximum(0);
            timeline.setValue(0);
            canvas.setResult(null);
            updateFrame();
            return;
        }
        selected = result;
        if (scenarioList.getSelectedValue() != result) {
            scenarioList.setSelectedValue(result, true);
        }
        int max = Math.max(0, result.ticksTrace().size() - 1);
        timeline.setMinimum(0);
        timeline.setMaximum(max);
        timeline.setValue(0);
        canvas.setResult(result);
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
        int index = Math.min(timeline.getValue(), Math.max(0, selected.ticksTrace().size() - 1));
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
}
