package fr.riege.ebsl.common.platform.render;

public final class WorldRender {
    private WorldRender() {
    }

    public static WorldRenderBuilder builder(RenderHandle handle) {
        return new WorldRenderBuilder(handle);
    }
}
