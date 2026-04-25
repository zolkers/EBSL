package fr.riege.ebsl.event.events.network;

import fr.riege.ebsl.event.Event;
import fr.riege.ebsl.packet.PacketDirection;
import net.minecraft.network.protocol.Packet;

public final class NetworkPacketEvent extends Event {
    private final PacketDirection direction;
    private final Packet<?> packet;

    public NetworkPacketEvent(PacketDirection direction, Packet<?> packet) {
        this.direction = direction;
        this.packet = packet;
    }

    public PacketDirection getDirection() { return direction; }
    public Packet<?> getPacket() { return packet; }
}
