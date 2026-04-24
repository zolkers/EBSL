package fr.riege.ebsl.ui.state;

public enum CenterTab {
    GAME("Game"),
    PATHFINDER_SETTINGS("Pathfinder Settings"),
    PACKET("Packet");

    private final String label;

    CenterTab(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
