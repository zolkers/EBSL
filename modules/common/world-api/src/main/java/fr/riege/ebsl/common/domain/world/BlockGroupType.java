package fr.riege.ebsl.common.domain.world;

public enum BlockGroupType {
    LEAF("leaf", "leaves"),
    WOOD("wood", "woods", "log", "logs", "stem", "stems", "hyphae"),
    GRASS("grass", "grasses");

    private final String[] tokens;

    BlockGroupType(String... tokens) {
        this.tokens = tokens;
    }

    public String[] tokens() {
        return tokens;
    }
}
