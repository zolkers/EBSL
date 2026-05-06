package fr.riege.ebsl.fabric;

import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import fr.riege.ebsl.loader.layer.MinecraftImGuiLayer;
import fr.riege.ebsl.loader.layer.MinecraftPhysicsLayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public class FabricEbslMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Minecraft client = Minecraft.getInstance();
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("ebsl");
        ModloaderCommonBootstrap.initialize(
            client,
            configDir,
            new MinecraftPhysicsLayer(client),
            new FabricCommandLayer(),
            new MinecraftImGuiLayer(client));

        ClientTickEvents.END_CLIENT_TICK.register(ignored -> ModloaderCommonBootstrap.tick());
    }
}
