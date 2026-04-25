package fr.riege.ebsl.botting.module;

import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.ColorSetting;
import fr.riege.ebsl.settings.EnumSetting;
import fr.riege.ebsl.settings.Settingable;
import fr.riege.ebsl.ui.layout.UiRect;
import imgui.ImDrawList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public final class KeyDisplayModule extends Settingable implements BotModule {
    public static final KeyDisplayModule INSTANCE = new KeyDisplayModule();

    private final BooleanSetting enabledSetting = registerSetting(
        new BooleanSetting("enabled", "Enabled", false));
    private final EnumSetting<KeyDisplayAnchor> anchorSetting = registerSetting(
        new EnumSetting<>("anchor", "Position", KeyDisplayAnchor.BOTTOM_LEFT, KeyDisplayAnchor.class));
    private final ColorSetting pressedColorSetting = registerSetting(
        new ColorSetting("pressed_color", "Pressed color", 0xCCDCEEFF));
    private final ColorSetting releasedColorSetting = registerSetting(
        new ColorSetting("released_color", "Released color", 0xCC131A24));

    private KeyDisplayModule() {}

    @Override public String id() { return "key_display"; }
    @Override public String displayName() { return "Key Display"; }
    @Override public String description() { return "Shows which movement keys the bot is pressing over the game viewport."; }
    @Override public BotModuleCategory category() { return BotModuleCategory.UTILITY; }
    @Override public boolean isEnabled() { return enabledSetting.value(); }
    @Override public void setEnabled(boolean enabled) { enabledSetting.setValue(enabled); }

    public void renderGameOverlay(ImDrawList dl, UiRect viewport) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Options opts = mc.options;

        boolean up    = opts.keyUp.isDown();
        boolean down  = opts.keyDown.isDown();
        boolean left  = opts.keyLeft.isDown();
        boolean right = opts.keyRight.isDown();
        boolean jump  = opts.keyJump.isDown();
        boolean sneak = opts.keyShift.isDown();

        float cell   = 28.0f;
        float gap    = 4.0f;
        float spaceW = cell * 3 + gap * 2;
        float spaceH = 20.0f;
        float sneakW = cell;
        float groupW = spaceW + gap + sneakW;
        float groupH = cell + gap + cell + gap + spaceH;
        float pad    = 12.0f;

        float x0 = anchoredX(viewport, groupW, pad);
        float y0 = anchoredY(viewport, groupH, pad);

        int pressedColor  = pressedColorSetting.value();
        int releasedColor = releasedColorSetting.value();

        // Row 1: up (centred over middle column)
        drawKey(dl, x0 + cell + gap, y0, cell, cell, up, 0, pressedColor, releasedColor);

        // Row 2: left / down / right
        float row2Y = y0 + cell + gap;
        drawKey(dl, x0,                      row2Y, cell, cell, left,  2, pressedColor, releasedColor);
        drawKey(dl, x0 + cell + gap,          row2Y, cell, cell, down,  1, pressedColor, releasedColor);
        drawKey(dl, x0 + cell * 2 + gap * 2, row2Y, cell, cell, right, 3, pressedColor, releasedColor);

        // Row 3: space + sneak
        float row3Y = row2Y + cell + gap;
        drawKey(dl, x0,                row3Y, spaceW, spaceH, jump,  -1, pressedColor, releasedColor);
        drawKey(dl, x0 + spaceW + gap, row3Y, sneakW, spaceH, sneak,  1, pressedColor, releasedColor);
    }

    private float anchoredX(UiRect vp, float groupW, float pad) {
        return switch (anchorSetting.value()) {
            case TOP_LEFT, BOTTOM_LEFT   -> vp.x() + pad;
            case TOP_CENTER, BOTTOM_CENTER -> vp.x() + (vp.width() - groupW) * 0.5f;
            case TOP_RIGHT, BOTTOM_RIGHT  -> vp.right() - groupW - pad;
        };
    }

    private float anchoredY(UiRect vp, float groupH, float pad) {
        return switch (anchorSetting.value()) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT       -> vp.y() + pad;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> vp.bottom() - groupH - pad;
        };
    }

    // dir: 0=up 1=down 2=left 3=right -1=none (space bar)
    private static void drawKey(ImDrawList dl, float x, float y, float w, float h,
                                boolean pressed, int dir, int pressedColor, int releasedColor) {
        int bg     = pressed ? pressedColor : releasedColor;
        int border = pressed ? 0xFFFFFFFF : 0xFF2E3C4E;
        int arrow  = pressed ? 0xFF0A1018 : 0xFF7A8898;

        dl.addRectFilled(x, y, x + w, y + h, bg, 4.0f);
        dl.addRect(x, y, x + w, y + h, border, 4.0f, 0, 1.0f);

        if (dir < 0) return;

        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float a  = 7.0f;

        switch (dir) {
            case 0 -> dl.addTriangleFilled(cx,     cy - a, cx - a, cy + a, cx + a, cy + a, arrow);
            case 1 -> dl.addTriangleFilled(cx,     cy + a, cx - a, cy - a, cx + a, cy - a, arrow);
            case 2 -> dl.addTriangleFilled(cx - a, cy,     cx + a, cy - a, cx + a, cy + a, arrow);
            case 3 -> dl.addTriangleFilled(cx + a, cy,     cx - a, cy - a, cx - a, cy + a, arrow);
        }
    }
}
