package fr.riege.ebsl.common.feature.scripting.highlight;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.ColorSetting;
import fr.riege.ebsl.common.core.settings.DoubleSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.settings.Settingable;

import java.util.List;

public final class EbslCodeEditorSettings extends Settingable {
    private static final EbslCodeEditorSettings INSTANCE = new EbslCodeEditorSettings();

    public final ColorSetting backgroundColor = registerSetting(new ColorSetting(
        "background_color", "Background", 0xFF0D1117));
    public final ColorSetting borderColor = registerSetting(new ColorSetting(
        "border_color", "Border", 0xFF26313D));
    public final ColorSetting gutterColor = registerSetting(new ColorSetting(
        "gutter_color", "Gutter", 0xFF111923));
    public final ColorSetting nativeTextColor = registerSetting(new ColorSetting(
        "native_text_color", "Native text", 0x00FFFFFF));
    public final ColorSetting frameColor = registerSetting(new ColorSetting(
        "frame_color", "Input frame", 0x00000000));
    public final ColorSetting caretColor = registerSetting(new ColorSetting(
        "caret_color", "Caret", 0xFFE6EDF3));
    public final DoubleSetting caretBlinkSeconds = registerSetting(new DoubleSetting(
        "caret_blink_seconds", "Caret blink seconds", 0.55, 0.0, 3.0));
    public final DoubleSetting caretThickness = registerSetting(new DoubleSetting(
        "caret_thickness", "Caret thickness", 1.4, 0.5, 6.0));
    public final BooleanSetting caretBlink = registerSetting(new BooleanSetting(
        "caret_blink", "Caret blink", true));
    public final DoubleSetting textPadding = registerSetting(new DoubleSetting(
        "text_padding", "Text padding", 8.0, 0.0, 24.0));

    private EbslCodeEditorSettings() {
    }

    public static EbslCodeEditorSettings instance() {
        return INSTANCE;
    }

    public static List<Setting<?>> all() {
        return INSTANCE.settings();
    }

    public static void resetToDefaults() {
        INSTANCE.resetSettings();
    }
}
