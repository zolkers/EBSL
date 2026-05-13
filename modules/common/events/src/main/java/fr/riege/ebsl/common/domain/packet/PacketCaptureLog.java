/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.domain.packet;

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

    public static void recordTrace(PacketCaptureEvent event) {
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

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }
    public static boolean isCaptureInbound() { return captureInbound; }
    public static void setCaptureInbound(boolean v) { captureInbound = v; }
    public static boolean isCaptureOutbound() { return captureOutbound; }
    public static void setCaptureOutbound(boolean v) { captureOutbound = v; }

    public static long inboundCount() { synchronized (LOCK) { return inboundCount; } }
    public static long outboundCount() { synchronized (LOCK) { return outboundCount; } }
}
