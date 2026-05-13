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

package fr.riege.ebsl.common.feature.terminal;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CommandSpecTest {
    @Test
    void builderCreatesMetaAndCompletionTogether() {
        CommandSpec spec = CommandSpec.named("demo")
            .description("Demo command")
            .bothScopes()
            .choices("mode", "walk", "wait", "warp")
            .argument("x")
            .executes(ctx -> CommandResult.ok("ok"))
            .build();

        assertEquals("demo", spec.meta().name());
        assertEquals("demo <walk|wait|warp> <x>", spec.meta().usage());
        assertEquals(CommandScope.BOTH, spec.meta().scope());
        assertEquals(List.of("walk", "wait", "warp"), spec.completer().suggest(0, "w"));
        assertTrue(spec.completer().suggest(1, "").isEmpty());
    }
}
