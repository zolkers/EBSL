package fr.riege.ebsl.common.feature.ui.imgui.settings;

import fr.riege.ebsl.common.core.settings.Setting;
import imgui.ImGui;
import imgui.type.ImString;

import java.util.Map;

public final class ImGuiSettingRenderContext {
    private final String ownerId;
    private final float itemWidth;
    private final Runnable onChanged;
    private final Map<String, ImString> textValues;

    public ImGuiSettingRenderContext(String ownerId, float itemWidth, Runnable onChanged, Map<String, ImString> textValues) {
        this.ownerId = ownerId;
        this.itemWidth = itemWidth;
        this.onChanged = onChanged;
        this.textValues = textValues;
    }

    public String label(Setting<?> setting) {
        return setting.displayName() + "##" + key(setting);
    }

    public String key(Setting<?> setting) {
        return ownerId + "." + setting.id();
    }

    public String key(Setting<?> setting, int index) {
        return key(setting) + "." + index;
    }

    public void applyItemWidth() {
        if (itemWidth > 0.0f) {
            ImGui.setNextItemWidth(itemWidth);
        }
    }

    public ImString textValue(String key, String current, int capacity) {
        ImString value = textValues.computeIfAbsent(key, ignored -> new ImString(current, capacity));
        if (!value.get().equals(current)) {
            value.set(current);
        }
        return value;
    }

    public void removeTextValue(String key) {
        textValues.remove(key);
    }

    public void changed() {
        onChanged.run();
    }
}
