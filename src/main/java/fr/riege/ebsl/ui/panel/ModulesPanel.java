package fr.riege.ebsl.ui.panel;

import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.botting.module.BotModule;
import fr.riege.ebsl.botting.module.BotModuleCategory;
import fr.riege.ebsl.botting.registry.BotModuleRegistry;
import fr.riege.ebsl.botting.storage.BotModuleSettingsStore;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.ui.EbslViewportScreen;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.RightPanelMode;
import net.minecraft.client.gui.GuiGraphics;

import java.util.EnumMap;
import java.util.Map;

public final class ModulesPanel implements UiPanel {
    @Override
    public void init(EbslViewportScreen screen, ViewportLayout layout) {
        UiRect panel = layout.right();
        if (screen.state().rightPanelMode() == RightPanelMode.MODULE_SETTINGS && screen.state().selectedModule() != null) {
            initModuleSettings(screen, panel);
            return;
        }
        initModuleList(screen, panel);
    }

    private void initModuleList(EbslViewportScreen screen, UiRect panel) {
        int y = panel.y() + 42;
        for (BotModule module : BotModuleRegistry.modules()) {
            screen.addButton(module.displayName(), panel.x() + UiTheme.PAD, y, panel.width() - UiTheme.PAD * 2, 22, button -> {
                screen.state().showModuleSettings(module);
                AnalyticsEventLog.record("module", "Opened settings for " + module.displayName());
                screen.rebuildUi();
            });
            y += 28;
        }
    }

    private void initModuleSettings(EbslViewportScreen screen, UiRect panel) {
        BotModule module = screen.state().selectedModule();
        int y = panel.y() + 42;
        screen.addButton("Back", panel.x() + UiTheme.PAD, y, 72, 22, button -> {
            screen.state().showModuleList();
            screen.rebuildUi();
        });
        screen.addButton("Reset to default", panel.right() - 134, y, 126, 22, button -> {
            module.resetSettings();
            BotModuleSettingsStore.save();
            AnalyticsEventLog.record("module", "Reset " + module.displayName());
            screen.rebuildUi();
        });

        y += 72;
        for (Setting<?> setting : module.settings()) {
            if (setting instanceof BooleanSetting boolSetting) {
                screen.addButton(boolSetting.value() ? "ON" : "OFF", panel.x() + 182, y, 76, 20, button -> {
                    boolSetting.setValue(!boolSetting.value());
                    BotModuleSettingsStore.save();
                    AnalyticsEventLog.record("setting", module.displayName() + "." + boolSetting.id() + "=" + boolSetting.value());
                    screen.rebuildUi();
                });
            } else if (setting instanceof IntSetting intSetting) {
                screen.addButton("-", panel.x() + 162, y, 28, 20, button -> {
                    intSetting.setValue(intSetting.value() - 1);
                    BotModuleSettingsStore.save();
                    AnalyticsEventLog.record("setting", module.displayName() + "." + intSetting.id() + "=" + intSetting.value());
                    screen.rebuildUi();
                });
                screen.addButton("+", panel.x() + 230, y, 28, 20, button -> {
                    intSetting.setValue(intSetting.value() + 1);
                    BotModuleSettingsStore.save();
                    AnalyticsEventLog.record("setting", module.displayName() + "." + intSetting.id() + "=" + intSetting.value());
                    screen.rebuildUi();
                });
            }
            y += 28;
        }
    }

    @Override
    public void render(EbslViewportScreen screen, GuiGraphics graphics, ViewportLayout layout,
                       int mouseX, int mouseY, float partialTick) {
        UiRect panel = layout.right();
        graphics.drawString(screen.fontRenderer(), "Bot modules", panel.x() + UiTheme.PAD, panel.y() + 16, UiTheme.TEXT, false);
        if (screen.state().rightPanelMode() == RightPanelMode.MODULE_SETTINGS && screen.state().selectedModule() != null) {
            renderModuleSettings(screen, graphics, panel);
            return;
        }
        renderModuleSummary(screen, graphics, panel);
    }

    private void renderModuleSummary(EbslViewportScreen screen, GuiGraphics graphics, UiRect panel) {
        Map<BotModuleCategory, Integer> counts = new EnumMap<>(BotModuleCategory.class);
        for (BotModule module : BotModuleRegistry.modules()) {
            counts.merge(module.category(), 1, Integer::sum);
        }
        int y = panel.bottom() - 88;
        graphics.drawString(screen.fontRenderer(), "Categories", panel.x() + UiTheme.PAD, y, UiTheme.TEXT, false);
        y += 16;
        for (BotModuleCategory category : BotModuleCategory.values()) {
            int count = counts.getOrDefault(category, 0);
            graphics.drawString(screen.fontRenderer(), category.displayName() + ": " + count,
                panel.x() + UiTheme.PAD, y, UiTheme.TEXT_DIM, false);
            y += 12;
        }
    }

    private void renderModuleSettings(EbslViewportScreen screen, GuiGraphics graphics, UiRect panel) {
        BotModule module = screen.state().selectedModule();
        graphics.drawString(screen.fontRenderer(), module.displayName(), panel.x() + UiTheme.PAD, panel.y() + 74, UiTheme.TEXT, false);
        graphics.drawString(screen.fontRenderer(), module.category().displayName(), panel.x() + UiTheme.PAD, panel.y() + 88, UiTheme.TEXT_DIM, false);
        int y = panel.y() + 114;
        for (Setting<?> setting : module.settings()) {
            graphics.drawString(screen.fontRenderer(), setting.displayName(), panel.x() + UiTheme.PAD, y + 6, UiTheme.TEXT_MUTED, false);
            if (setting instanceof IntSetting intSetting) {
                graphics.drawCenteredString(screen.fontRenderer(), Integer.toString(intSetting.value()), panel.x() + 210, y + 6, UiTheme.TEXT);
            }
            y += 28;
        }
    }
}
