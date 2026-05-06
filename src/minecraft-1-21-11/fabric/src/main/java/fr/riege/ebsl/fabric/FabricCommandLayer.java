package fr.riege.ebsl.fabric;

import fr.riege.ebsl.loader.layer.MinecraftCommandLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class FabricCommandLayer extends MinecraftCommandLayer<FabricClientCommandSource> {
    public FabricCommandLayer() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerAll(dispatcher));
    }
}
