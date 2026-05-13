package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Map;

/**
 * Defines the contract for {@code GoalUiExecutor} implementations.
 */
@FunctionalInterface
public interface GoalUiExecutor {
    int execute(NavigationService navigation, Map<String, Integer> values);
}
