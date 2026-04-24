package fr.riege.ebsl.command.goal;

import java.util.Map;

@FunctionalInterface
public interface GoalUiExecutor {
    int execute(Map<String, Integer> values);
}
