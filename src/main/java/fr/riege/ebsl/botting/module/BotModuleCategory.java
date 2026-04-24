package fr.riege.ebsl.botting.module;

public enum BotModuleCategory {
    FARMING("Farming"),
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    UTILITY("Utility");

    private final String displayName;

    BotModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
