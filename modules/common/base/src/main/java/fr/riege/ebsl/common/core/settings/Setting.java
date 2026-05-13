package fr.riege.ebsl.common.core.settings;

import com.google.gson.JsonElement;

/**
 * Defines the contract for {@code Setting} implementations.
 */
public interface Setting<T> {
    String id();

    String displayName();

    T value();

    T defaultValue();

    void setValue(T value);

    void resetToDefault();

    JsonElement toJson();

    void load(JsonElement json);
}
