package fr.riege.ebsl.botting.module;

import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.event.Subscription;
import fr.riege.ebsl.event.events.render.RenderGameViewportEvent;
import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.EnumSetting;
import fr.riege.ebsl.settings.Settingable;
import fr.riege.ebsl.ui.layout.UiRect;
import imgui.ImDrawList;
import net.minecraft.client.Minecraft;

public final class MoveTypeOverlayModule extends Settingable implements PathfinderModule {
    public static final MoveTypeOverlayModule INSTANCE = new MoveTypeOverlayModule();

    private final BooleanSetting enabledSetting = registerSetting(
        new BooleanSetting("enabled", "Enabled", false));
    private final EnumSetting<KeyDisplayAnchor> anchorSetting = registerSetting(
        new EnumSetting<>("anchor", "Position", KeyDisplayAnchor.TOP_LEFT, KeyDisplayAnchor.class));

    private Subscription renderSubscription;
    private EventBus bus;

    private MoveTypeOverlayModule() {}

    @Override public String id()          { return "move_type_overlay"; }
    @Override public String displayName() { return "Move Type Overlay"; }
    @Override public String description() { return "Shows the current pathfinder movement type while navigating."; }
    @Override public PathfinderModuleCategory category() { return PathfinderModuleCategory.RENDER; }
    @Override public boolean isEnabled()  { return enabledSetting.value(); }
    @Override public void setEnabled(boolean enabled) { enabledSetting.setValue(enabled); }

    @Override
    public void onEnable(EventBus eventBus) {
        this.bus = eventBus;
        renderSubscription = eventBus.subscribe(RenderGameViewportEvent.class, this::onRender);
    }

    @Override
    public void onDisable() {
        if (renderSubscription != null && bus != null) {
            bus.unsubscribe(renderSubscription);
            renderSubscription = null;
        }
    }

    private void onRender(RenderGameViewportEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!PathfindingManager.isNavigating()) return;

        Node.MoveType moveType = PathfindingManager.getCurrentMoveType();
        if (moveType == null) return;

        render(event.getDrawList(), event.getViewport(), moveType);
    }

    private void render(ImDrawList dl, UiRect viewport, Node.MoveType moveType) {
        String label = moveType.name();
        float pad  = 8.0f;
        float textH = 10.0f;
        float boxW  = label.length() * 6.5f + pad * 2;
        float boxH  = textH + pad * 2;

        float x0 = anchoredX(viewport, boxW, 12.0f);
        float y0 = anchoredY(viewport, boxH, 12.0f);

        int bg     = moveTypeColor(moveType);
        int border = blendBorderFrom(bg);

        dl.addRectFilled(x0, y0, x0 + boxW, y0 + boxH, bg, 4.0f);
        dl.addRect      (x0, y0, x0 + boxW, y0 + boxH, border, 4.0f, 0, 1.0f);
        dl.addText(x0 + pad, y0 + pad, 0xFFEEF4FF, label);
    }

    private float anchoredX(UiRect vp, float w, float pad) {
        return switch (anchorSetting.value()) {
            case TOP_LEFT, BOTTOM_LEFT     -> vp.x() + pad;
            case TOP_CENTER, BOTTOM_CENTER -> vp.x() + (vp.width() - w) * 0.5f;
            case TOP_RIGHT, BOTTOM_RIGHT   -> vp.right() - w - pad;
        };
    }

    private float anchoredY(UiRect vp, float h, float pad) {
        return switch (anchorSetting.value()) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT          -> vp.y() + pad;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> vp.bottom() - h - pad;
        };
    }

    private static int moveTypeColor(Node.MoveType type) {
        return switch (type) {
            case WALK, WALK_DIAGONAL -> 0xCC0E1C2E;
            case STEP_UP             -> 0xCC1A3320;
            case JUMP                -> 0xCC1A2A00;
            case PARKOUR             -> 0xCC2D1A00;
            case FALL                -> 0xCC2A1A00;
            case SWIM                -> 0xCC00202E;
            case CLIMB               -> 0xCC1A1A2E;
            case FLY                 -> 0xCC1A001A;
        };
    }

    private static int blendBorderFrom(int bg) {
        int r = Math.min(255, ((bg >> 16) & 0xFF) + 60);
        int g = Math.min(255, ((bg >>  8) & 0xFF) + 60);
        int b = Math.min(255,  (bg        & 0xFF) + 60);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}
