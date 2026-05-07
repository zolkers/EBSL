package fr.riege.ebsl.common.core.threading;

public enum EbslThreadDomain {
    PATHFINDING("pathfinding"),
    MODULES("modules"),
    TASKS("tasks"),
    RENDERING("rendering"),
    IO("io"),
    GENERAL("general");

    private final String id;

    EbslThreadDomain(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
