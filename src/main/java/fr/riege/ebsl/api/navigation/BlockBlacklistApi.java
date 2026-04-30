package fr.riege.ebsl.api.navigation;

import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.general.module.BlockBlacklist;
import fr.riege.ebsl.general.module.PathfinderBlockBlacklistModule;
import fr.riege.ebsl.general.registry.BotModuleRegistry;
import fr.riege.ebsl.general.storage.BotModuleSettingsStore;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

@EbslApiSurface(EbslApiSurface.Domain.NAVIGATION)
public final class BlockBlacklistApi {
    BlockBlacklistApi() {
    }

    @EbslApiOperation("Check whether block blacklist filtering is active.")
    public boolean enabled() {
        return BlockBlacklist.isEnabled();
    }

    @EbslApiOperation("Read configured blacklisted block ids as GUI strings.")
    public List<String> configuredBlockIds() {
        return PathfinderBlockBlacklistModule.INSTANCE.blockIds();
    }

    @EbslApiOperation("Read parsed blacklisted block identifiers.")
    public Set<Identifier> parsedBlockIds() {
        return BlockBlacklist.ids();
    }

    @EbslApiOperation("Enable or disable pathfinder block blacklist filtering.")
    public void setEnabled(boolean enabled) {
        PathfinderBlockBlacklistModule module = PathfinderBlockBlacklistModule.INSTANCE;
        module.setEnabled(enabled);
        BotModuleSettingsStore.save();
        BotModuleRegistry.syncLifecycle(module);
    }

    @EbslApiOperation("Replace the configured pathfinder block blacklist.")
    public void setConfiguredBlockIds(List<String> blockIds) {
        PathfinderBlockBlacklistModule module = PathfinderBlockBlacklistModule.INSTANCE;
        module.setBlockIds(blockIds);
        BotModuleSettingsStore.save();
        BotModuleRegistry.onSettingChanged(module, module.blockIdsSetting());
    }

    @EbslApiOperation("Check whether a block state is currently blacklisted.")
    public boolean contains(BlockState state) {
        return BlockBlacklist.isBlacklisted(state);
    }
}
