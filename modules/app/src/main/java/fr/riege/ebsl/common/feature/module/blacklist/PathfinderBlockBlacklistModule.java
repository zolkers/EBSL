package fr.riege.ebsl.common.feature.module.blacklist;

import fr.riege.ebsl.common.platform.layer.IEventBus;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.module.PathfinderModuleCategory;
import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.settings.Settingable;
import fr.riege.ebsl.common.core.settings.StringListSetting;
import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.pathfinding.block.BlockBlacklist;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PathfinderBlockBlacklistModule extends Settingable implements PathfinderModule {
    public static final PathfinderBlockBlacklistModule INSTANCE = new PathfinderBlockBlacklistModule();

    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final StringListSetting blockIds = registerSetting(new StringListSetting(
        "blocks",
        "Blocks",
        List.of("minecraft:barrier")));

    private Set<BlockId> parsedBlockIds = parseBlockIds(blockIds.value());

    private PathfinderBlockBlacklistModule() {
    }

    @Override public String id() { return "pathfinder_block_blacklist"; }
    @Override public String displayName() { return "Pathfinder Block Blacklist"; }
    @Override public String description() { return "Prevents the pathfinder from using selected blocks."; }
    @Override public PathfinderModuleCategory category() { return PathfinderModuleCategory.BEHAVIOUR; }
    @Override public boolean isEnabled() { return enabledSetting.value(); }

    @Override public void setEnabled(boolean enabled) {
        enabledSetting.setValue(enabled);
        syncBlacklist();
    }

    @Override public void onEnable(IEventBus bus) {
        syncBlacklist();
    }

    @Override public void onDisable() {
        syncBlacklist();
    }

    @Override public void onSettingChanged(Setting<?> setting) {
        if (setting == blockIds) {
            parsedBlockIds = parseBlockIds(blockIds.value());
        }
        syncBlacklist();
    }

    private void syncBlacklist() {
        parsedBlockIds = parseBlockIds(blockIds.value());
        BlockBlacklist.update(isEnabled(), isEnabled() ? parsedBlockIds : Set.of());
    }

    private static Set<BlockId> parseBlockIds(List<String> raw) {
        Set<BlockId> ids = new HashSet<>();
        if (raw == null || raw.isEmpty()) return ids;
        for (String entry : raw) {
            if (entry == null) continue;
            for (String token : entry.split("[,;\\s]+")) {
                BlockId id = parseBlockId(token);
                if (id != null) ids.add(id);
            }
        }
        return ids;
    }

    private static BlockId parseBlockId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }
        return BlockId.of(normalized);
    }
}
