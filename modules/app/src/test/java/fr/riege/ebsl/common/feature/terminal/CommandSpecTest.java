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
