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
        CoreRegistries.events().register(TickEvent.class);
        CoreRegistries.events().register(RenderWorldEvent.class);
        CoreRegistries.events().register(RenderHudEvent.class);
        CoreRegistries.events().register(KeyPressEvent.class);
        CoreRegistries.events().register(MouseButtonEvent.class);
        CoreRegistries.events().register(CharTypedEvent.class);
        CoreRegistries.events().register(ScaledMousePosEvent.class);
        CoreRegistries.events().register(BlitToScreenEvent.class);
        CoreRegistries.events().register(GrabMouseEvent.class);
        CoreRegistries.events().register(PacketCaptureEvent.class);
    }
}
