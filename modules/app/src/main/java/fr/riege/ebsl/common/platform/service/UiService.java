package fr.riege.ebsl.common.platform.service;

/**
 * Controls the visibility of the in-game EBSL user interface.
 *
 * <p>Platform adapters implement this boundary so commands and modules can toggle UI state without depending on loader details.</p>
 */
public interface UiService {
    /**
     * Toggles UI visibility and returns the new visible state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean toggle();

    /**
     * Returns whether visible is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isVisible();
}
