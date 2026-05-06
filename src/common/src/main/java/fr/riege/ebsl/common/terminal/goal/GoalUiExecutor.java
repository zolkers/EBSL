package fr.riege.ebsl.common.terminal.goal;

import fr.riege.ebsl.common.service.NavigationService;

import java.util.Map;

@FunctionalInterface
public interface GoalUiExecutor {
    int execute(NavigationService navigation, Map<String, Integer> values);
}
