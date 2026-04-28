package fr.riege.ebsl.botting.module;

import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.settings.Settingable;
import fr.riege.ebsl.settings.StringListSetting;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PathfinderBlockBlacklistModule extends Settingable implements PathfinderModule {
    public static final PathfinderBlockBlacklistModule INSTANCE = new PathfinderBlockBlacklistModule();

    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final StringListSetting blockIds = registerSetting(new StringListSetting(
        "blocks",
        "Blocks",
        List.of("minecraft:slime_block")));

    private Set<Identifier> parsedBlockIds = parseBlockIds(blockIds.value());

    private PathfinderBlockBlacklistModule() {}

    @Override public String id() { return "pathfinder_block_blacklist"; }
    @Override public String displayName() { return "Pathfinder Block Blacklist"; }
    @Override public String description() { return "Prevents the pathfinder from using selected blocks."; }
    @Override public PathfinderModuleCategory category() { return PathfinderModuleCategory.BEHAVIOUR; }
    @Override public boolean isEnabled() { return enabledSetting.value(); }
    @Override public void setEnabled(boolean enabled) {
        enabledSetting.setValue(enabled);
        syncBlacklist();
    }

    public List<String> blockIds() {
        return blockIds.value();
    }

    public StringListSetting blockIdsSetting() {
        return blockIds;
    }

    public void setBlockIds(List<String> rawBlockIds) {
        blockIds.setValue(rawBlockIds);
        parsedBlockIds = parseBlockIds(blockIds.value());
        syncBlacklist();
    }

    @Override
    public void onEnable(EventBus bus) {
        syncBlacklist();
    }

    @Override
    public void onDisable() {
        syncBlacklist();
    }

    @Override
    public void onSettingChanged(Setting<?> setting) {
        if (setting == blockIds) {
            parsedBlockIds = parseBlockIds(blockIds.value());
        }
        syncBlacklist();
    }

    private void syncBlacklist() {
        BlockBlacklist.update(isEnabled(), isEnabled() ? parsedBlockIds : Set.of());
    }

    private static Set<Identifier> parseBlockIds(List<String> raw) {
        Set<Identifier> ids = new HashSet<>();
        if (raw == null || raw.isEmpty()) return ids;
        for (String entry : raw) {
            Identifier id = Identifier.tryParse(entry.trim());
            if (id != null) ids.add(id);
        }
        return ids;
    }
}
