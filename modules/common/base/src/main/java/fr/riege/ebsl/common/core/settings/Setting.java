package fr.riege.ebsl.common.core.settings;

import com.google.gson.JsonElement;

/**
 * Represents one configurable value exposed by modules, tasks, or UI components.
 *
 * <p>Settings carry identity, display metadata, current/default values, and JSON persistence behavior.</p>
 */
public interface Setting<T> {
    /**
     * Returns the stable identifier used for lookup, persistence, and diagnostics.
 *
     * @return the value defined by this contract
     */
    String id();

    /**
     * Returns the human-readable name shown in UI and help surfaces.
 *
     * @return the value defined by this contract
     */
    String displayName();

    T value();

    T defaultValue();

    /**
     * Updates the current setting value.
 *
     * @param value the value to apply
     */
    void setValue(T value);

    /**
     * Restores this setting to its default value.
     */
    void resetToDefault();

    /**
     * Serializes this setting to a JSON value.
 *
     * @return the value defined by this contract
     */
    JsonElement toJson();

    /**
     * Loads this setting from a JSON value.
 *
     * @param json the serialized JSON payload
     */
    void load(JsonElement json);
}
