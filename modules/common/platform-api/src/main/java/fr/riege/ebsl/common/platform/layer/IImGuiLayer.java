package fr.riege.ebsl.common.platform.layer;

/**
 * Provides the platform ImGui frame integration.
 *
 * <p>The layer registers panel drawing callbacks and reports the active viewport dimensions for layout code.</p>
 */
public interface IImGuiLayer {
    /**
     * Registers the callback invoked for each ImGui frame.
 *
     * @param drawPanels the callback that draws registered panels
     */
    void registerFrame(Runnable drawPanels);
    /**
     * Returns the current ImGui viewport width in pixels.
 *
     * @return the value defined by this contract
     */
    int getViewportWidth();
    /**
     * Returns the current ImGui viewport height in pixels.
 *
     * @return the value defined by this contract
     */
    int getViewportHeight();
}
