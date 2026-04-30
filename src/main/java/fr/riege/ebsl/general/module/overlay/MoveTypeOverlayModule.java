package fr.riege.ebsl.general.module.overlay;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.api.navigation.NavigationSnapshot;
import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.event.Subscription;
import fr.riege.ebsl.event.events.render.RenderGameViewportEvent;
import fr.riege.ebsl.general.module.PathfinderModule;
import fr.riege.ebsl.general.module.PathfinderModuleCategory;
import fr.riege.ebsl.pathfinding.Node;
import net.minecraft.client.Minecraft;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.EnumSetting;
import fr.riege.ebsl.settings.Settingable;
import fr.riege.ebsl.ui.layout.UiRect;
import imgui.ImDrawList;

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
        NavigationSnapshot snapshot = EbslApi.navigation().snapshot();
        if (!snapshot.navigating()) return;

        Node.MoveType moveType = snapshot.currentMoveType();
        if (moveType == null) return;

        render(event.getDrawList(), event.getViewport(), moveType);
    }

    private void render(ImDrawList dl, UiRect viewport, Node.MoveType moveType) {
        String label = moveType.name();
        float pad   = 12.0f;
        float textH = 10.0f;
        float boxW  = label.length() * 6.5f + pad * 2;
        float boxH  = textH + pad * 2;

        float x0 = anchorSetting.value().x(viewport, boxW, pad);
        float y0 = anchorSetting.value().y(viewport, boxH, pad);

        dl.addRectFilled(x0, y0, x0 + boxW, y0 + boxH, 0xCC101820, 4.0f);
        dl.addRect      (x0, y0, x0 + boxW, y0 + boxH, 0xFF2E3C4E, 4.0f, 0, 1.0f);
        dl.addText(x0 + pad, y0 + pad, 0xFFDDEEFF, label);
    }

}
