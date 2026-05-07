package fr.riege.ebsl.common.feature.ui.state;

public enum MainViewTab {
    MAIN("Main");

    private final String label;

    MainViewTab(String label) { this.label = label; }

    public String label() { return label; }
}
