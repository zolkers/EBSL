package fr.riege.ebsl.general.module;

public enum PathfinderModuleCategory {
    RENDER("Render"),
    BEHAVIOUR("Behaviour");

    private final String displayName;

    PathfinderModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
