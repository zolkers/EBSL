package fr.riege.ebsl.common.packet;

public record PacketCaptureEvent(
    long sequence,
    long capturedAtMs,
    PacketDirection direction,
    String packetId,
    String packetClass,
    boolean terminal,
    boolean skippable
) {
}
