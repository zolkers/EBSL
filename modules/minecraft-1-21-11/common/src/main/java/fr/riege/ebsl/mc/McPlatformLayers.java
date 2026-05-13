package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.platform.layer.IRenderLayer;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import fr.riege.ebsl.common.world.layer.IEntityLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Groups Minecraft-backed platform layer contracts.
 */
public record McPlatformLayers(
    IWorldLayer world,
    IPlayerLayer player,
    IRenderLayer render,
    IStorageLayer storage,
    IEntityLayer entities
) {
    /**
     * Creates the platform layer set for a Minecraft client.
     *
     * @param client the Minecraft client
     * @param configDir the configuration directory used by storage
     * @return Minecraft-backed platform layers exposed through contracts
     */
    public static McPlatformLayers create(Minecraft client, Path configDir) {
        Minecraft effectiveClient = Objects.requireNonNull(client, "client");
        return new McPlatformLayers(
            new McWorldLayer(effectiveClient),
            new McPlayerLayer(effectiveClient),
            new McRenderLayer(),
            new McStorageLayer(Objects.requireNonNull(configDir, "configDir")),
            new McEntityLayer(effectiveClient));
    }
}
