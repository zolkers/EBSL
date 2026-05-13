package fr.riege.ebsl.common.platform.layer;

/**
 * Defines the contract for {@code IImGuiLayer} implementations.
 */
public interface IImGuiLayer {
    void registerFrame(Runnable drawPanels);
    int getViewportWidth();
    int getViewportHeight();
}
