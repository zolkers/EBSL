package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.core.settings.Setting;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Defines the runtime contract for one script graph node.
 *
 * <p>A node owns its settings, argument loading, execution lifecycle, and completion policy so scripts can mix navigation, input, and control-flow actions consistently.</p>
 */
public interface EbslNode {
    /**
     * Returns the stable identifier used for lookup, persistence, and diagnostics.
 *
     * @return the value defined by this contract
     */
    String id();

    /**
     * Returns alternate script names accepted for this node.
 *
     * @return the requested values
     */
    default List<String> aliases() {
        return List.of();
    }

    /**
     * Returns whether this script node should wait for navigation to finish before completing.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean waitsForNavigation() {
        return false;
    }

    /**
     * Returns whether this node releases gameplay keys when it finishes.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean releasesGameplayKeys() {
        return false;
    }

    /**
     * Returns whether this node behaves as a wait-until control-flow node.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isWaitUntil() {
        return false;
    }

    /**
     * Returns the mutable settings exposed by this component.
 *
     * @return the requested values
     */
    default List<Setting<?>> settings() {
        return List.of();
    }

    /**
     * Returns editable fields exposed by this node.
 *
     * @return the requested values
     */
    default List<EbslNodeField> fields() {
        List<Setting<?>> settings = settings();
        return IntStream.range(0, settings.size())
            .mapToObj(index -> EbslNodeField.fromSetting(id(), index, settings.get(index)))
            .toList();
    }

    /**
     * Loads textual script arguments into this node state.
 *
     * @param args the command or script arguments
     */
    default void loadArgs(List<String> args) {
    }

    /**
     * Serializes this node settings back into script argument text.
 *
     * @return the value defined by this contract
     */
    default String argsFromSettings() {
        return "";
    }

    /**
     * Starts  behavior.
 *
     * @param invocation the invocation state for the current script or parser operation
     * @return the value defined by this contract
     */
    int start(EbslNodeInvocation invocation);

    /**
     * Advances this component by one runtime tick.
 *
     * @param invocation the invocation state for the current script or parser operation
     */
    default void tick(EbslNodeInvocation invocation) {
    }

    /**
     * Returns whether this node has completed its current invocation.
 *
     * @param invocation the invocation state for the current script or parser operation
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isComplete(EbslNodeInvocation invocation) {
        return false;
    }

    /**
     * Finishes this node invocation and performs any required cleanup.
 *
     * @param invocation the invocation state for the current script or parser operation
     */
    default void finish(EbslNodeInvocation invocation) {
    }
}
