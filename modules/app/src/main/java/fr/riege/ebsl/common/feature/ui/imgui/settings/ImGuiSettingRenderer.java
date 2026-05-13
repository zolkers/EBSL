package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.Setting;

/**
 * Renders one setting type inside ImGui configuration panels.
 *
 * <p>Implementations declare the setting class they support and update values through the provided render context.</p>
 */
interface ImGuiSettingRenderer {
    /**
     * Returns the concrete setting type rendered by this renderer.
 *
     * @return the requested values
     */
    Class<? extends Setting<?>> settingType();

    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @param setting the setting being rendered or updated
     * @param context the context describing the operation being performed
     */
    void render(Setting<?> setting, ImGuiSettingRenderContext context);
}
