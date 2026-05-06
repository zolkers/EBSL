package fr.riege.ebsl.common.settings;

import com.google.gson.JsonElement;

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
