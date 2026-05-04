package fr.riege.ebsl.general.task;

public enum MobTargetMode {
    CLOSEST_MOB("Closest mob"),
    ENTITY_NAME("Entity name");

    private final String displayName;

    MobTargetMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
