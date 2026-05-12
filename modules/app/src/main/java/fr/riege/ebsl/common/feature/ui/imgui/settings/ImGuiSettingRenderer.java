package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.Setting;

interface ImGuiSettingRenderer {
    Class<? extends Setting<?>> settingType();

    void render(Setting<?> setting, ImGuiSettingRenderContext context);
}
