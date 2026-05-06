package fr.riege.ebsl.common.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ColorSetting extends AbstractSetting<Integer> {
    public ColorSetting(String id, String displayName, int defaultValue) {
        super(id, displayName, defaultValue);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(String.format("#%08X", value()));
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        try {
            String s = json.getAsString().replace("#", "");
            setValue((int) Long.parseLong(s, 16));
        } catch (NumberFormatException ignored) {
        }
    }
}
