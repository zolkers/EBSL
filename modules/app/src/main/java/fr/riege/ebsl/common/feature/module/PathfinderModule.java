package fr.riege.ebsl.common.feature.module;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.layer.IEventBus;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.List;

/**
 * Describes a toggleable pathfinder-facing feature module.
 *
 * <p>Modules expose identity, settings, lifecycle hooks, and optional viewport rendering without tying the registry to concrete implementations.</p>
 */
public interface PathfinderModule {
    /**
     * Returns the stable identifier used for lookup, persistence, and diagnostics.
 *
     * @return the value defined by this contract
     */
    String id();
    /**
     * Returns the human-readable name shown in UI and help surfaces.
 *
     * @return the value defined by this contract
     */
    String displayName();
    /**
     * Returns a concise human-readable description of this component.
 *
     * @return the value defined by this contract
     */
    String description();
    /**
     * Returns the module category used by UI grouping.
 *
     * @return the value defined by this contract
     */
    PathfinderModuleCategory category();
    /**
     * Returns whether this component is currently enabled.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isEnabled();
    /**
     * Updates whether this component is enabled.
 *
     * @param enabled whether the component should be enabled
     */
    void setEnabled(boolean enabled);
    /**
     * Returns the mutable settings exposed by this component.
 *
     * @return the requested values
     */
    List<Setting<?>> settings();
    /**
     * Restores every setting owned by this component to its default value.
     */
    void resetSettings();

    /**
     * Handles activation after the component has been enabled.
 *
     * @param bus the event bus available while the module is enabled
     */
    default void onEnable(IEventBus bus) {}
    /**
     * Handles cleanup immediately before or after the component is disabled.
     */
    default void onDisable() {}
    /**
     * Handles notification that one of the component settings changed.
 *
     * @param setting the setting being rendered or updated
     */
    default void onSettingChanged(Setting<?> setting) {}
    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @param platform the platform services available to the component
     * @param navigation the navigation service used by the component
     * @param viewport the viewport region available for rendering
     */
    default void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {}
    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean renderGameViewportAsync() { return false; }
}
