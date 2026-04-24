package fr.riege.ebsl.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StringListSetting extends AbstractSetting<List<String>> {
    public StringListSetting(String id, String displayName, List<String> defaultValue) {
        super(id, displayName, List.copyOf(defaultValue));
    }

    @Override
    public void setValue(List<String> value) {
        super.setValue(sanitize(value));
    }

    public void setEntry(int index, String value) {
        List<String> next = new ArrayList<>(value());
        next.set(index, value);
        setValue(next);
    }

    public void addEntry(String value) {
        List<String> next = new ArrayList<>(value());
        next.add(value);
        setValue(next);
    }

    public void removeEntry(int index) {
        List<String> next = new ArrayList<>(value());
        next.remove(index);
        setValue(next);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (String entry : value()) {
            array.add(entry);
        }
        return array;
    }

    @Override
    public void load(JsonElement json) {
        if (json == null) {
            return;
        }
        if (json.isJsonPrimitive()) {
            setValue(List.of(json.getAsString().split("[,\\n]")));
            return;
        }
        if (!json.isJsonArray()) {
            return;
        }
        List<String> values = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray()) {
            if (element != null && element.isJsonPrimitive()) {
                values.add(element.getAsString());
            }
        }
        setValue(values);
    }

    private static List<String> sanitize(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> sanitized = new ArrayList<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            sanitized.add(value.trim());
        }
        return Collections.unmodifiableList(sanitized);
    }
}
