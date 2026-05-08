package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.Setting;

import java.util.List;

public final class ImGuiSettingRendererRegistry {
    private static final List<ImGuiSettingRenderer> RENDERERS = List.of(
        new BooleanSettingRenderer(),
        new IntSettingRenderer(),
        new DoubleSettingRenderer(),
        new StringSettingRenderer(),
        new ColorSettingRenderer(),
        new EnumSettingRenderer(),
        new StringListSettingRenderer()
    );

    private ImGuiSettingRendererRegistry() {
    }

    public static boolean render(Setting<?> setting, ImGuiSettingRenderContext context) {
        for (ImGuiSettingRenderer renderer : RENDERERS) {
            if (renderer.settingType().isInstance(setting)) {
                renderer.render(setting, context);
                return true;
            }
        }
        return false;
    }
}
