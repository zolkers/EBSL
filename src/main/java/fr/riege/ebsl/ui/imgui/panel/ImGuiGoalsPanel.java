package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.command.GoalCommandSupport;
import fr.riege.ebsl.command.GoalCommands;
import fr.riege.ebsl.command.GoalRegistry;
import fr.riege.ebsl.command.goal.GoalParameter;
import fr.riege.ebsl.command.goal.GoalUiDefinition;
import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ImGuiGoalsPanel implements ImGuiUiPanel {
    private final Map<String, ImString> values = new HashMap<>();
    private GoalUiDefinition selected;

    @Override
    public void render(EbslUiState state, ViewportLayout layout) {
        if (selected == null) {
            GoalCommands.bootstrap();
            selected = GoalRegistry.uiDefinitions().getFirst();
            fillDefaults(selected);
        }

        ImGuiPanelUtil.nextFixedWindow(layout.left());
        if (ImGui.begin("Goals##ebsl-left", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Goals");
            ImGui.separator();
            renderGoalList();
            ImGui.separator();
            renderSelectedGoal();
            ImGui.end();
        }
    }

    private void renderGoalList() {
        List<GoalUiDefinition> goals = GoalRegistry.uiDefinitions();
        if (ImGui.beginChild("##goal-list", 0.0f, 160.0f, true)) {
            for (GoalUiDefinition goal : goals) {
                boolean isSelected = selected != null && selected.id().equals(goal.id());
                if (ImGui.selectable(goal.label(), isSelected)) {
                    selected = goal;
                    fillDefaults(goal);
                }
            }
            ImGui.endChild();
        }
    }

    private void renderSelectedGoal() {
        if (selected == null) {
            return;
        }
        ImGui.text(selected.label());
        if (!selected.description().isBlank()) {
            ImGui.textDisabled(selected.description());
        }
        ImGui.spacing();
        for (GoalParameter parameter : selected.parameters()) {
            ImString value = values.computeIfAbsent(parameter.id(), id -> new ImString(16));
            ImGui.inputText(parameter.label(), value);
        }
        if (ImGui.button("Use defaults", 112.0f, 24.0f)) {
            fillDefaults(selected);
        }
        ImGui.sameLine();
        if (ImGui.button("Go", 76.0f, 24.0f)) {
            startSelectedGoal();
        }
    }

    private void fillDefaults(GoalUiDefinition definition) {
        Minecraft minecraft = Minecraft.getInstance();
        for (GoalParameter parameter : definition.parameters()) {
            values.computeIfAbsent(parameter.id(), id -> new ImString(16))
                .set(Integer.toString(parameter.defaultValue(minecraft)));
        }
    }

    private void startSelectedGoal() {
        Map<String, Integer> parsed = new HashMap<>();
        for (GoalParameter parameter : selected.parameters()) {
            Integer value = parseInt(values.get(parameter.id()).get());
            if (value == null) {
                GoalCommandSupport.sendClientMessage("Invalid value for " + parameter.label() + ".");
                AnalyticsEventLog.record("goal", "Invalid value for " + selected.id() + "." + parameter.id());
                return;
            }
            parsed.put(parameter.id(), value);
        }
        int result = selected.execute(parsed);
        if (result > 0) {
            AnalyticsEventLog.record("goal", "Started " + selected.label());
        }
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
