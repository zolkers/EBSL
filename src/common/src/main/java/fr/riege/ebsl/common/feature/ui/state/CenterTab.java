package fr.riege.ebsl.common.feature.ui.state;

public enum CenterTab {
    GAME("Game"),
    PATHFINDER_SETTINGS("Pathfinder Settings"),
    RENDER_SETTINGS("Render"),
    PACKET("Packet"),
    TERMINAL("Terminal"),
    MC_LOG("MC Log");

    private final String label;

    CenterTab(String label) { this.label = label; }

    public String label() { return label; }
}
