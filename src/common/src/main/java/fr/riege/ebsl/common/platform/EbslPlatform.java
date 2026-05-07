package fr.riege.ebsl.common.platform;

import fr.riege.ebsl.common.platform.layer.*;

public record EbslPlatform(
    IWorldLayer world,
    IPlayerLayer player,
    IPhysicsLayer physics,
    IEventBus events,
    IRenderLayer render,
    ICommandLayer commands,
    IStorageLayer storage,
    IImGuiLayer imgui,
    IInputLayer input,
    IEntityLayer entities
) {
    public EbslPlatform {
        if (world == null)    throw new NullPointerException("world layer required");
        if (player == null)   throw new NullPointerException("player layer required");
        if (physics == null)  throw new NullPointerException("physics layer required");
        if (events == null)   throw new NullPointerException("events layer required");
        if (render == null)   throw new NullPointerException("render layer required");
        if (commands == null) throw new NullPointerException("commands layer required");
        if (storage == null)  throw new NullPointerException("storage layer required");
        if (imgui == null)    throw new NullPointerException("imgui layer required");
        if (input == null)    throw new NullPointerException("input layer required");
        if (entities == null) throw new NullPointerException("entity layer required");
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private IWorldLayer world;
        private IPlayerLayer player;
        private IPhysicsLayer physics;
        private IEventBus events;
        private IRenderLayer render;
        private ICommandLayer commands;
        private IStorageLayer storage;
        private IImGuiLayer imgui;
        private IInputLayer input;
        private IEntityLayer entities = new IEntityLayer() {};

        public Builder world(IWorldLayer v)      { this.world = v;    return this; }
        public Builder player(IPlayerLayer v)    { this.player = v;   return this; }
        public Builder physics(IPhysicsLayer v)  { this.physics = v;  return this; }
        public Builder events(IEventBus v)       { this.events = v;   return this; }
        public Builder render(IRenderLayer v)    { this.render = v;   return this; }
        public Builder commands(ICommandLayer v) { this.commands = v; return this; }
        public Builder storage(IStorageLayer v)  { this.storage = v;  return this; }
        public Builder imgui(IImGuiLayer v)      { this.imgui = v;    return this; }
        public Builder input(IInputLayer v)      { this.input = v;    return this; }
        public Builder entities(IEntityLayer v)  { this.entities = v; return this; }

        public EbslPlatform build() {
            return new EbslPlatform(world, player, physics, events, render, commands, storage, imgui, input, entities);
        }
    }
}
