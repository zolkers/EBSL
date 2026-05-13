/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.core.event;

import fr.riege.ebsl.common.core.event.events.input.GrabMouseEvent;
import fr.riege.ebsl.common.domain.packet.PacketCaptureEvent;

public final class CommonEventTypes {
    private static boolean registered;

    private CommonEventTypes() {
    }

    public static void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;
        EventRegistry.register(TickEvent.class);
        EventRegistry.register(RenderWorldEvent.class);
        EventRegistry.register(RenderHudEvent.class);
        EventRegistry.register(KeyPressEvent.class);
        EventRegistry.register(MouseButtonEvent.class);
        EventRegistry.register(CharTypedEvent.class);
        EventRegistry.register(ScaledMousePosEvent.class);
        EventRegistry.register(BlitToScreenEvent.class);
        EventRegistry.register(GrabMouseEvent.class);
        EventRegistry.register(PacketCaptureEvent.class);
    }
}
