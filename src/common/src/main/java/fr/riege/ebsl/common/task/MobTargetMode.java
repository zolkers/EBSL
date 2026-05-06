package fr.riege.ebsl.common.task;

public enum MobTargetMode {
    CLOSEST_MOB("Closest mob"),
    ENTITY_NAME("Entity name");

    private final String label;

    MobTargetMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
