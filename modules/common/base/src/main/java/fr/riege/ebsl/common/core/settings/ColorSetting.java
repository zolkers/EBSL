package fr.riege.ebsl.common.core.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ColorSetting extends AbstractSetting<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-settings");

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
        } catch (NumberFormatException exception) {
            LOGGER.debug("Ignoring invalid color setting '{}': {}", id(), json, exception);
        }
    }
}
