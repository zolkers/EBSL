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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

public final class SimulationFrame extends JFrame {
    private final List<SimulationResult> results;
    private final ReplayCanvas canvas = new ReplayCanvas();
    private final JSlider timeline = new JSlider();
    private final JLabel status = new JLabel("ready");
    private final Timer timer;
    private SimulationResult selected;

    private SimulationFrame(List<SimulationResult> results) {
        super("EBSL Pathfinder Simulator");
        this.results = List.copyOf(results);
        this.timer = new Timer(50, event -> stepForward());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 720));
        setLayout(new BorderLayout());
        add(toolbar(), BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(timelinePanel(), BorderLayout.SOUTH);
        selectResult(this.results.isEmpty() ? null : this.results.getFirst());
        pack();
        setLocationRelativeTo(null);
    }

    public static void show(List<SimulationResult> results) {
        SwingUtilities.invokeLater(() -> new SimulationFrame(results).setVisible(true));
    }

    private JPanel toolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JComboBox<SimulationResult> scenarios = new JComboBox<>();
        scenarios.setModel(new DefaultComboBoxModel<>(results.toArray(SimulationResult[]::new)));
        scenarios.setRenderer(new SimulationResultRenderer());
        scenarios.addActionListener(event -> selectResult((SimulationResult) scenarios.getSelectedItem()));
        JButton play = new JButton("Play");
        play.addActionListener(event -> {
            if (timer.isRunning()) {
                timer.stop();
                play.setText("Play");
            } else {
                timer.start();
                play.setText("Pause");
            }
        });
        JButton reset = new JButton("Reset");
        reset.addActionListener(event -> {
            timer.stop();
            play.setText("Play");
            timeline.setValue(0);
            updateFrame();
        });
        panel.add(new JLabel("Scenario"));
        panel.add(scenarios);
        panel.add(play);
        panel.add(reset);
        panel.add(status);
        return panel;
    }

    private JPanel timelinePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        timeline.addChangeListener(event -> updateFrame());
        panel.add(timeline, BorderLayout.CENTER);
        return panel;
    }

    private void selectResult(SimulationResult result) {
        selected = result;
        int max = result == null ? 0 : Math.max(0, result.ticksTrace().size() - 1);
        timeline.setMinimum(0);
        timeline.setMaximum(max);
        timeline.setValue(0);
        canvas.setResult(result);
        updateFrame();
    }

    private void stepForward() {
        if (timeline.getValue() >= timeline.getMaximum()) {
            timer.stop();
            return;
        }
        timeline.setValue(timeline.getValue() + 1);
    }

    private void updateFrame() {
        canvas.setFrame(timeline.getValue());
        if (selected == null) {
            status.setText("no replay");
            return;
        }
        status.setText("tick " + timeline.getValue()
            + " / " + Math.max(0, selected.ticksTrace().size() - 1)
            + " | status " + selected.status()
            + " | stuck " + selected.metrics().stuckTicks());
    }
}
