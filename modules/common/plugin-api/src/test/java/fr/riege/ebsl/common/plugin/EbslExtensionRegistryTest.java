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

package fr.riege.ebsl.common.plugin;

import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.layer.ICommandLayer;
import fr.riege.ebsl.common.platform.layer.IEventBus;
import fr.riege.ebsl.common.platform.layer.IImGuiLayer;
import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.layer.IRenderLayer;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EbslExtensionRegistryTest {
    private static final EbslExtensionPoint<Runnable> RUNNABLES =
        EbslExtensionPoint.of("test.runnable", Runnable.class);

    @Test
    void storesContributionsInOwnerOrder() {
        EbslExtensionRegistry registry = new EbslExtensionRegistry();
        Runnable first = () -> {};
        Runnable second = () -> {};

        registry.register(new EbslExtensionDescriptor("late", "Late", "1", 20), RUNNABLES, second);
        registry.register(new EbslExtensionDescriptor("early", "Early", "1", 10), RUNNABLES, first);

        assertEquals(List.of(first, second), registry.contributions(RUNNABLES));
        assertEquals(2, registry.size(RUNNABLES));
    }

    @Test
    void rejectsContributionWithWrongType() {
        EbslExtensionPoint<CharSequence> strings = EbslExtensionPoint.of("test.string", CharSequence.class);
        EbslExtensionDescriptor owner = EbslExtensionDescriptor.of("owner", "Owner");

        assertThrows(IllegalArgumentException.class, () -> new EbslContribution(owner, strings, 42));
    }

    @Test
    void bootstrapsExtensionsInDescriptorOrder() {
        EbslExtensionRegistry registry = new EbslExtensionRegistry();
        EbslExtensionContext context = new EbslExtensionContext(platform(), registry);
        List<String> calls = new ArrayList<>();
        EbslExtension late = extension("late", 20, () -> calls.add("late"));
        EbslExtension early = extension("early", 10, () -> calls.add("early"));

        assertSame(registry, EbslExtensions.bootstrap(context, List.of(late, early)));

        assertEquals(List.of("early", "late"), calls);
    }

    @Test
    void validatesExtensionPointIdentity() {
        assertThrows(IllegalArgumentException.class, () -> EbslExtensionPoint.of(" ", Runnable.class));
        assertThrows(NullPointerException.class, () -> EbslExtensionPoint.of("missing_type", null));
        assertThrows(IllegalArgumentException.class, () -> EbslExtensionDescriptor.of("", ""));
        assertTrue(new EbslExtensionRegistry().isEmpty());
    }

    private static EbslExtension extension(String id, int order, Runnable action) {
        return new EbslExtension() {
            @Override
            public EbslExtensionDescriptor descriptor() {
                return new EbslExtensionDescriptor(id, id, "1", order);
            }

            @Override
            public void contribute(EbslExtensionContext context) {
                action.run();
            }
        };
    }

    private static EbslPlatform platform() {
        return EbslPlatform.builder()
            .world(proxy(IWorldLayer.class))
            .player(proxy(IPlayerLayer.class))
            .physics(proxy(IPhysicsLayer.class))
            .events(proxy(IEventBus.class))
            .render(proxy(IRenderLayer.class))
            .commands(proxy(ICommandLayer.class))
            .storage(proxy(IStorageLayer.class))
            .imgui(proxy(IImGuiLayer.class))
            .input(proxy(IInputLayer.class))
            .build();
    }

    private static <T> T proxy(Class<T> type) {
        Object proxy = Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] { type },
            (ignored, method, args) -> switch (method.getReturnType().getName()) {
                case "boolean" -> false;
                case "int" -> 0;
                case "long" -> 0L;
                case "float" -> 0.0f;
                case "double" -> 0.0;
                default -> null;
            });
        return type.cast(proxy);
    }
}
