package fr.riege.ebsl.common.ui.imgui.panel;

import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.terminal.goal.GoalParameter;
import fr.riege.ebsl.common.terminal.goal.GoalUiCatalog;
import fr.riege.ebsl.common.terminal.goal.GoalUiDefinition;
import fr.riege.ebsl.common.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ImGuiGoalsPanel implements ImGuiUiPanel {
    private final Map<String, ImString> values = new HashMap<>();
    private GoalUiDefinition selected;

    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation) {
        if (selected == null) {
            List<GoalUiDefinition> goals = GoalUiCatalog.all();
            if (goals.isEmpty()) return;
            selected = goals.getFirst();
            fillDefaults(selected);
        }

        ImGuiPanelUtil.nextFixedWindow(layout.left());
        if (ImGui.begin("Goals##ebsl-left", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Goals");
            ImGui.separator();
            renderGoalList();
            ImGui.separator();
            renderSelectedGoal(navigation);
            ImGui.end();
        }
    }

    private void renderGoalList() {
        if (ImGui.beginChild("##goal-list", 0.0f, 160.0f, true)) {
            for (GoalUiDefinition goal : GoalUiCatalog.all()) {
                boolean isSelected = selected != null && selected.id().equals(goal.id());
                if (ImGui.selectable(goal.label(), isSelected)) {
                    selected = goal;
                    fillDefaults(goal);
                }
            }
            ImGui.endChild();
        }
    }

    private void renderSelectedGoal(NavigationService navigation) {
        if (selected == null) return;
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
            startSelectedGoal(navigation);
        }
        ImGui.pushStyleColor(ImGuiCol.Button,        0xFF8A2630);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0xFFA8323E);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive,  0xFFC23B49);
        if (ImGui.button("Stop##goal-stop", -1.0f, 24.0f)) {
            navigation.stop(false);
        }
        ImGui.popStyleColor(3);
    }

    private void fillDefaults(GoalUiDefinition definition) {
        IPlayerLayer player = EbslServices.platform().player();
        for (GoalParameter parameter : definition.parameters()) {
            values.computeIfAbsent(parameter.id(), id -> new ImString(16))
                .set(Integer.toString(parameter.defaultValue(player)));
        }
    }

    private void startSelectedGoal(NavigationService navigation) {
        Map<String, Integer> parsed = new HashMap<>();
        for (GoalParameter parameter : selected.parameters()) {
            Integer value = parseInt(values.get(parameter.id()).get());
            if (value == null) return;
            parsed.put(parameter.id(), value);
        }
        selected.execute(navigation, parsed);
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
