package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.ColorSetting;
import fr.riege.ebsl.common.core.settings.DoubleSetting;
import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImInt;

import java.util.List;

final class ImGuiSettingControls {
    private ImGuiSettingControls() {
    }

    static void renderGroup(String label, List<Setting<?>> settings) {
        ImGui.setNextItemOpen(true, ImGuiCond.Once);
        if (!ImGui.collapsingHeader(label)) return;
        ImGui.indent(10.0f);
        for (Setting<?> setting : settings) render(setting);
        ImGui.unindent(10.0f);
        ImGui.spacing();
    }

    static void reset(List<Setting<?>> settings) {
        for (Setting<?> setting : settings) {
            setting.resetToDefault();
        }
        PathfinderSettings.save();
    }

    private static void render(Setting<?> setting) {
        if (setting instanceof BooleanSetting s) {
            ImBoolean v = new ImBoolean(s.value());
            if (ImGui.checkbox(setting.displayName(), v)) {
                s.setValue(v.get());
                PathfinderSettings.save();
            }
        } else if (setting instanceof IntSetting s) {
            ImInt v = new ImInt(s.value());
            if (ImGui.inputInt(setting.displayName(), v)) {
                s.setValue(clamp(v.get(), s.min(), s.max()));
                PathfinderSettings.save();
            }
        } else if (setting instanceof DoubleSetting s) {
            ImDouble v = new ImDouble(s.value());
            if (ImGui.inputDouble(setting.displayName(), v, 0.1, 1.0, "%.3f")) {
                s.setValue(clamp(v.get(), s.min(), s.max()));
                PathfinderSettings.save();
            }
        } else if (setting instanceof ColorSetting s) {
            renderColor(s);
        }
    }

    private static void renderColor(ColorSetting setting) {
        int argb = setting.value();
        float[] col = {
            ((argb >> 16) & 0xFF) / 255.0f,
            ((argb >> 8) & 0xFF) / 255.0f,
            (argb & 0xFF) / 255.0f,
            ((argb >> 24) & 0xFF) / 255.0f
        };
        if (ImGui.colorEdit4(setting.displayName(), col)) {
            int packed = ((int) (col[3] * 255) << 24)
                | ((int) (col[0] * 255) << 16)
                | ((int) (col[1] * 255) << 8)
                | (int) (col[2] * 255);
            setting.setValue(packed);
            PathfinderSettings.save();
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
