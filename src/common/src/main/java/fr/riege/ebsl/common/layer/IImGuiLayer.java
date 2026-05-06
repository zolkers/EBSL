package fr.riege.ebsl.common.layer;

public interface IImGuiLayer {
    void registerFrame(Runnable drawPanels);
    int getViewportWidth();
    int getViewportHeight();
}
