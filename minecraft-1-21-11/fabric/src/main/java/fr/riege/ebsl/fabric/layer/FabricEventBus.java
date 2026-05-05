package fr.riege.ebsl.fabric.layer;

import fr.riege.ebsl.common.event.*;
import fr.riege.ebsl.common.layer.IEventBus;
import java.util.function.Consumer;

public class FabricEventBus implements IEventBus {
    @Override public void onTick(Consumer<TickEvent> handler) { throw new UnsupportedOperationException("TODO"); }
    @Override public void onRenderWorld(Consumer<RenderWorldEvent> handler) { throw new UnsupportedOperationException("TODO"); }
    @Override public void onRenderHud(Consumer<RenderHudEvent> handler) { throw new UnsupportedOperationException("TODO"); }
    @Override public void onKeyPress(Consumer<KeyPressEvent> handler) { throw new UnsupportedOperationException("TODO"); }
    @Override public void onMouseButton(Consumer<MouseButtonEvent> handler) { throw new UnsupportedOperationException("TODO"); }
    @Override public void onCharTyped(Consumer<CharTypedEvent> handler) { throw new UnsupportedOperationException("TODO"); }
}
