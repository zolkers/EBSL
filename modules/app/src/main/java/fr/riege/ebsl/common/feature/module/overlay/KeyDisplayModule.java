package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.core.settings.ColorSetting;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import imgui.ImDrawList;
import imgui.ImGui;

@SuppressWarnings({"java:S107", "java:S6548"})
public final class KeyDisplayModule extends AbstractAnchoredOverlayModule {
    public static final KeyDisplayModule INSTANCE = new KeyDisplayModule();

    private final ColorSetting pressedColorSetting = registerSetting(
        new ColorSetting("pressed_color", "Pressed color", 0xCCDCEEFF));
    private final ColorSetting releasedColorSetting = registerSetting(
        new ColorSetting("released_color", "Released color", 0xCC131A24));

    private KeyDisplayModule() {
        super(KeyDisplayAnchor.BOTTOM_LEFT);
    }

    @Override public String id() { return "key_display"; }
    @Override public String displayName() { return "Key Display"; }
    @Override public String description() { return "Shows which movement keys the bot is pressing over the game viewport."; }

    @Override
    public void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {
        if (!isEnabled()) return;
        render(ImGui.getForegroundDrawList(), viewport, platform);
    }

    private void render(ImDrawList dl, UiRect viewport, EbslPlatform platform) {
        boolean up = platform.input().forwardDown();
        boolean down = platform.input().backwardDown();
        boolean left = platform.input().leftDown();
        boolean right = platform.input().rightDown();
        boolean jump = platform.input().jumpDown();
        boolean sneak = platform.input().sneakDown();

        float sneakW = 28.0f;
        float gap = 4.0f;
        float spaceW = sneakW * 3 + gap * 2;
        float spaceH = 20.0f;
        float groupW = spaceW + gap + sneakW;
        float groupH = sneakW + gap + sneakW + gap + spaceH;
        float pad = 12.0f;

        float x0 = anchor().x(viewport, groupW, pad);
        float y0 = anchor().y(viewport, groupH, pad);

        int pressedColor = pressedColorSetting.value();
        int releasedColor = releasedColorSetting.value();

        drawKey(dl, x0 + sneakW + gap, y0, sneakW, sneakW, up, 0, pressedColor, releasedColor);
        float row2Y = y0 + sneakW + gap;
        drawKey(dl, x0, row2Y, sneakW, sneakW, left, 2, pressedColor, releasedColor);
        drawKey(dl, x0 + sneakW + gap, row2Y, sneakW, sneakW, down, 1, pressedColor, releasedColor);
        drawKey(dl, x0 + sneakW * 2 + gap * 2, row2Y, sneakW, sneakW, right, 3, pressedColor, releasedColor);
        float row3Y = row2Y + sneakW + gap;
        drawKey(dl, x0, row3Y, spaceW, spaceH, jump, -1, pressedColor, releasedColor);
        drawKey(dl, x0 + spaceW + gap, row3Y, sneakW, spaceH, sneak, 1, pressedColor, releasedColor);
    }

    private static void drawKey(ImDrawList dl, float x, float y, float w, float h,
                                boolean pressed, int dir, int pressedColor, int releasedColor) {
        int bg = pressed ? pressedColor : releasedColor;
        int border = pressed ? 0xFFFFFFFF : 0xFF2E3C4E;
        int arrow = pressed ? 0xFF0A1018 : 0xFF7A8898;

        dl.addRectFilled(x, y, x + w, y + h, bg, 4.0f);
        dl.addRect(x, y, x + w, y + h, border, 4.0f, 0, 1.0f);
        if (dir < 0) return;

        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float a = 7.0f;
        switch (dir) {
            case 0 -> dl.addTriangleFilled(cx, cy - a, cx - a, cy + a, cx + a, cy + a, arrow);
            case 1 -> dl.addTriangleFilled(cx, cy + a, cx - a, cy - a, cx + a, cy - a, arrow);
            case 2 -> dl.addTriangleFilled(cx - a, cy, cx + a, cy - a, cx + a, cy + a, arrow);
            case 3 -> dl.addTriangleFilled(cx + a, cy, cx - a, cy - a, cx - a, cy + a, arrow);
            default -> {
                // Spacebar uses dir -1 and has no directional arrow.
            }
        }
    }
}
