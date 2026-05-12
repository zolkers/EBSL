package fr.riege.ebsl.common.domain.world;

public enum BlockSelectorOperator {
    OR("|"),
    AND("&"),
    NOT("!");

    private final String token;

    BlockSelectorOperator(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }
}
