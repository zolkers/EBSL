package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.Setting;

/**
 * Defines the contract for {@code ImGuiSettingRenderer} implementations.
 */
interface ImGuiSettingRenderer {
    Class<? extends Setting<?>> settingType();

    void render(Setting<?> setting, ImGuiSettingRenderContext context);
}
