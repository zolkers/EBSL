package fr.riege.ebsl.packet;

public enum PacketDirection {
    INBOUND("S2C"),
    OUTBOUND("C2S");

    private final String label;

    PacketDirection(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
