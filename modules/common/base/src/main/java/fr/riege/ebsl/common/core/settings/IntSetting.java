package fr.riege.ebsl.common.core.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class IntSetting extends AbstractSetting<Integer> {
    private final int min;
    private final int max;

    public IntSetting(String id, String displayName, int defaultValue, int min, int max) {
        super(id, displayName, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(Math.clamp(value, min, max));
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value());
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            setValue(json.getAsInt());
        }
    }
}
