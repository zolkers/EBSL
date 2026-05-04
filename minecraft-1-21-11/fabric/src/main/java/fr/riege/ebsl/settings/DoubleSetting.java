package fr.riege.ebsl.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class DoubleSetting extends AbstractSetting<Double> {
    private final double min;
    private final double max;

    public DoubleSetting(String id, String displayName, double defaultValue, double min, double max) {
        super(id, displayName, defaultValue);
        this.min = min;
        this.max = max;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    @Override
    public void setValue(Double value) {
        super.setValue(Math.max(min, Math.min(max, value)));
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value());
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            setValue(json.getAsDouble());
        }
    }
}
