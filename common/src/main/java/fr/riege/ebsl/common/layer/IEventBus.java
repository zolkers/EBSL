package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.event.TickEvent;
import fr.riege.ebsl.common.event.RenderWorldEvent;
import fr.riege.ebsl.common.event.RenderHudEvent;
import fr.riege.ebsl.common.event.KeyPressEvent;
import fr.riege.ebsl.common.event.MouseButtonEvent;
import fr.riege.ebsl.common.event.CharTypedEvent;
import java.util.function.Consumer;

public interface IEventBus {
    void onTick(Consumer<TickEvent> handler);
    void onRenderWorld(Consumer<RenderWorldEvent> handler);
    void onRenderHud(Consumer<RenderHudEvent> handler);
    void onKeyPress(Consumer<KeyPressEvent> handler);
    void onMouseButton(Consumer<MouseButtonEvent> handler);
    void onCharTyped(Consumer<CharTypedEvent> handler);
}
