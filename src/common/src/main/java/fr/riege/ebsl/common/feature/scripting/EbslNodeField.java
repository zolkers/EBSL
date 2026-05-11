package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeFieldHelp;

public record EbslNodeField(
    int argumentIndex,
    String id,
    String label,
    String type,
    String defaultValue,
    String description,
    Setting<?> setting
) {
    public static EbslNodeField fromSetting(String command, int argumentIndex, Setting<?> setting) {
        return new EbslNodeField(
            argumentIndex,
            setting.id(),
            setting.displayName(),
            EbslNodeFieldHelp.typeName(setting),
            EbslNodeFieldHelp.value(setting.defaultValue()),
            EbslNodeFieldHelp.description(command, setting),
            setting
        );
    }
}
