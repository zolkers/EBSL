package fr.riege.ebsl.packet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PacketCaptureLog {
    private static final int MAX_EVENTS = 1000;
    private static final Object LOCK = new Object();
    private static final Deque<PacketCaptureEvent> EVENTS = new ArrayDeque<>(MAX_EVENTS);

    private static boolean enabled = true;
    private static boolean captureInbound = true;
    private static boolean captureOutbound = true;
    private static long nextSequence;
    private static long inboundCount;
    private static long outboundCount;

    private PacketCaptureLog() {
    }

    public static void record(PacketDirection direction, Packet<?> packet) {
        if (!enabled || packet == null || !isDirectionEnabled(direction)) {
            return;
        }

        PacketType<?> type = packet.type();
        PacketCaptureEvent event = new PacketCaptureEvent(
            ++nextSequence,
            System.currentTimeMillis(),
            direction,
            type != null && type.id() != null ? type.id().toString() : "unknown",
            packet.getClass().getSimpleName(),
            packet.isTerminal(),
            packet.isSkippable());

        synchronized (LOCK) {
            if (EVENTS.size() >= MAX_EVENTS) {
                EVENTS.removeFirst();
            }
            EVENTS.addLast(event);
            if (direction == PacketDirection.INBOUND) {
                inboundCount++;
            } else {
                outboundCount++;
            }
        }
    }

    public static List<PacketCaptureEvent> snapshot() {
        synchronized (LOCK) {
            return new ArrayList<>(EVENTS);
        }
    }

    public static void clear() {
        synchronized (LOCK) {
            EVENTS.clear();
            inboundCount = 0;
            outboundCount = 0;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        PacketCaptureLog.enabled = enabled;
    }

    public static boolean isCaptureInbound() {
        return captureInbound;
    }

    public static void setCaptureInbound(boolean captureInbound) {
        PacketCaptureLog.captureInbound = captureInbound;
    }

    public static boolean isCaptureOutbound() {
        return captureOutbound;
    }

    public static void setCaptureOutbound(boolean captureOutbound) {
        PacketCaptureLog.captureOutbound = captureOutbound;
    }

    public static long inboundCount() {
        synchronized (LOCK) {
            return inboundCount;
        }
    }

    public static long outboundCount() {
        synchronized (LOCK) {
            return outboundCount;
        }
    }

    private static boolean isDirectionEnabled(PacketDirection direction) {
        return direction == PacketDirection.INBOUND ? captureInbound : captureOutbound;
    }
}
