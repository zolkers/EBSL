package fr.riege.ebsl.common.packet;

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

    private PacketCaptureLog() {}

    public static void record(PacketCaptureEvent event) {
        if (!enabled) return;
        if (event.direction() == PacketDirection.INBOUND && !captureInbound) return;
        if (event.direction() == PacketDirection.OUTBOUND && !captureOutbound) return;

        synchronized (LOCK) {
            if (EVENTS.size() >= MAX_EVENTS) EVENTS.removeFirst();
            EVENTS.addLast(event);
            if (event.direction() == PacketDirection.INBOUND) inboundCount++;
            else outboundCount++;
        }
    }

    public static PacketCaptureEvent buildEvent(PacketDirection direction, String packetId, String packetClass,
                                                 boolean terminal, boolean skippable) {
        return new PacketCaptureEvent(++nextSequence, System.currentTimeMillis(),
            direction, packetId, packetClass, terminal, skippable);
    }

    public static List<PacketCaptureEvent> snapshot() {
        synchronized (LOCK) { return new ArrayList<>(EVENTS); }
    }

    public static void clear() {
        synchronized (LOCK) {
            EVENTS.clear();
            inboundCount = 0;
            outboundCount = 0;
        }
    }

    public static boolean isEnabled()            { return enabled; }
    public static void setEnabled(boolean v)     { enabled = v; }
    public static boolean isCaptureInbound()     { return captureInbound; }
    public static void setCaptureInbound(boolean v)  { captureInbound = v; }
    public static boolean isCaptureOutbound()    { return captureOutbound; }
    public static void setCaptureOutbound(boolean v) { captureOutbound = v; }

    public static long inboundCount()  { synchronized (LOCK) { return inboundCount; } }
    public static long outboundCount() { synchronized (LOCK) { return outboundCount; } }
}
