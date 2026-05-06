package fr.riege.ebsl.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BooleanSetting extends AbstractSetting<Boolean> {
    public BooleanSetting(String id, String displayName, boolean defaultValue) {
        super(id, displayName, defaultValue);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value());
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            setValue(json.getAsBoolean());
        }
    }
}
