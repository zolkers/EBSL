package fr.riege.ebsl.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class EnumSetting<E extends Enum<E>> extends AbstractSetting<E> {
    private final Class<E> type;

    public EnumSetting(String id, String displayName, E defaultValue, Class<E> type) {
        super(id, displayName, defaultValue);
        this.type = type;
    }

    public Class<E> enumType() {
        return type;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value().name());
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        try {
            setValue(Enum.valueOf(type, json.getAsString()));
        } catch (IllegalArgumentException ignored) {
        }
    }
}
