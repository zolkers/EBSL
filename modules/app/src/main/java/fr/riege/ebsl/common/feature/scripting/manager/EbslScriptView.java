package fr.riege.ebsl.common.feature.scripting.manager;

public enum EbslScriptView {
    GRAPH("Graph"),
    CODE("Code");

    private final String label;

    EbslScriptView(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
