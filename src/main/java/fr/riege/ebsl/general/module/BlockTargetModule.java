package fr.riege.ebsl.general.module;

import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.event.Subscription;
import fr.riege.ebsl.event.events.render.RenderGameViewportEvent;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.EnumSetting;
import fr.riege.ebsl.settings.Settingable;
import fr.riege.ebsl.ui.layout.UiRect;
import imgui.ImDrawList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class BlockTargetModule extends Settingable implements PathfinderModule {
    public static final BlockTargetModule INSTANCE = new BlockTargetModule();

    private final BooleanSetting enabledSetting = registerSetting(
        new BooleanSetting("enabled", "Enabled", false));
    private final EnumSetting<KeyDisplayAnchor> anchorSetting = registerSetting(
        new EnumSetting<>("anchor", "Position", KeyDisplayAnchor.TOP_RIGHT, KeyDisplayAnchor.class));

    private Subscription renderSubscription;
    private EventBus bus;

    private BlockTargetModule() {}

    @Override public String id()            { return "block_target"; }
    @Override public String displayName()   { return "Block Target"; }
    @Override public String description()   { return "Shows the resource ID of the block currently looked at."; }
    @Override public PathfinderModuleCategory category() { return PathfinderModuleCategory.RENDER; }
    @Override public boolean isEnabled()    { return enabledSetting.value(); }
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
        if (mc.player == null || mc.level == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit)) return;
        if (hit.getType() == HitResult.Type.MISS) return;

        String id = BuiltInRegistries.BLOCK.getKey(
            mc.level.getBlockState(hit.getBlockPos()).getBlock()).toString();

        render(event.getDrawList(), event.getViewport(), id);
    }

    private void render(ImDrawList dl, UiRect viewport, String id) {
        float pad  = 12.0f;
        float textH = 10.0f;
        float boxW  = id.length() * 6.5f + pad * 2;
        float boxH  = textH + pad * 2;

        float x0 = anchorSetting.value().x(viewport, boxW, pad);
        float y0 = anchorSetting.value().y(viewport, boxH, pad);

        dl.addRectFilled(x0, y0, x0 + boxW, y0 + boxH, 0xCC101820, 4.0f);
        dl.addRect      (x0, y0, x0 + boxW, y0 + boxH, 0xFF2E3C4E, 4.0f, 0, 1.0f);
        dl.addText(x0 + pad, y0 + pad, 0xFFDDEEFF, id);
    }

}
