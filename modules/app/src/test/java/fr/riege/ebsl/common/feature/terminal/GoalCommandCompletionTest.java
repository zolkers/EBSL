package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.feature.terminal.commands.GoalCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GoalCommandCompletionTest {
    @Test
    void suggestsGoalIdsForFirstArgument() {
        CommandCompletion completion = GoalCommand.spec().completer();

        List<String> suggestions = completion.suggest(List.of(), 0, "w");

        assertTrue(suggestions.contains("walk"));
        assertTrue(suggestions.contains("walkxz"));
    }

    @Test
    void suggestsParametersFromSelectedGoalDefinition() {
        CommandCompletion completion = GoalCommand.spec().completer();

        assertEquals(List.of("0"), completion.suggest(List.of("walk"), 1, ""));
        assertEquals(List.of("0"), completion.suggest(List.of("walk", "12"), 2, ""));
        assertEquals(List.of("0"), completion.suggest(List.of("walk", "12", "64"), 3, ""));
        assertTrue(completion.suggest(List.of("walk", "12", "64", "12"), 4, "").isEmpty());
    }

    @Test
    void suggestsConstantParameterDefaultsForLaterGoalArgs() {
        CommandCompletion completion = GoalCommand.spec().completer();

        assertEquals(List.of("2"), completion.suggest(List.of("near", "10", "64", "10"), 4, ""));
    }

    @Test
    void legacyAliasUsesCanonicalGoalParameters() {
        CommandCompletion completion = GoalCommand.spec().completer();

        assertEquals(List.of("0"), completion.suggest(List.of("block"), 1, ""));
        assertEquals(List.of("0"), completion.suggest(List.of("block", "10", "64"), 3, ""));
    }
}
