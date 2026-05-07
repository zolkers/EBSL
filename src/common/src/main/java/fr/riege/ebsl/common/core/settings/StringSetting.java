package fr.riege.ebsl.common.core.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class StringSetting extends AbstractSetting<String> {
    public StringSetting(String id, String displayName, String defaultValue) {
        super(id, displayName, defaultValue);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value());
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            setValue(json.getAsString());
        }
    }
}
