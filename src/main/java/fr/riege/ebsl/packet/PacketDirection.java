package fr.riege.ebsl.packet;

public enum PacketDirection {
    INBOUND("S -> C"),
    OUTBOUND("C -> S");

    private final String label;

    PacketDirection(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
