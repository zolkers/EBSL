package fr.riege.ebsl.api.gui;

import fr.riege.ebsl.settings.Setting;

import java.util.List;

public record GuiSettingsGroup(String label, List<Setting<?>> settings) {
}
