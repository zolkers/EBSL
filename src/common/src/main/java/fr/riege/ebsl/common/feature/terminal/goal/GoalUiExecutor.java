package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Map;

@FunctionalInterface
public interface GoalUiExecutor {
    int execute(NavigationService navigation, Map<String, Integer> values);
}
