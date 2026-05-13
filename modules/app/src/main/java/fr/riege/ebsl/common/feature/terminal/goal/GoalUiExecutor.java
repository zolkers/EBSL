package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Map;

/**
 * Executes a goal request assembled from UI-provided parameters.
 *
 * <p>Implementations translate validated integer inputs into navigation service calls and return command-style status codes.</p>
 */
@FunctionalInterface
public interface GoalUiExecutor {
    /**
     * Executes the operation represented by this contract.
 *
     * @param navigation the navigation service used by the component
     * @param values the validated parameter values
     * @return the value defined by this contract
     */
    int execute(NavigationService navigation, Map<String, Integer> values);
}
