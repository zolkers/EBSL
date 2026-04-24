package fr.riege.ebsl.botting.module;

import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.settings.Settingable;
import fr.riege.ebsl.settings.StringSetting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public final class PathfinderBlockBlacklistModule extends Settingable implements BotModule {
    public static final PathfinderBlockBlacklistModule INSTANCE = new PathfinderBlockBlacklistModule();

    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final StringSetting blockIds = registerSetting(new StringSetting(
        "blocks",
        "Blocks",
        "minecraft:slime_block"));
    private Set<Identifier> parsedBlockIds = parseBlockIds(blockIds.value());

    private PathfinderBlockBlacklistModule() {
    }

    @Override
    public String id() {
        return "pathfinder_block_blacklist";
    }

    @Override
    public String displayName() {
        return "Pathfinder Block Blacklist";
    }

    @Override
    public String description() {
        return "Prevents the pathfinder from using selected blocks.";
    }

    @Override
    public BotModuleCategory category() {
        return BotModuleCategory.UTILITY;
    }

    @Override
    public boolean isEnabled() {
        return enabledSetting.value();
    }

    @Override
    public void setEnabled(boolean enabled) {
        enabledSetting.setValue(enabled);
    }

    @Override
    public void onSettingChanged(Setting<?> setting) {
        if (setting == blockIds) {
            parsedBlockIds = parseBlockIds(blockIds.value());
        }
    }

    public static boolean isBlacklisted(BlockState state) {
        if (!INSTANCE.isEnabled() || state == null || state.isAir()) {
            return false;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id != null && INSTANCE.parsedBlockIds.contains(id);
    }

    private static Set<Identifier> parseBlockIds(String raw) {
        Set<Identifier> ids = new HashSet<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        for (String entry : raw.split("[,\\n]")) {
            Identifier id = Identifier.tryParse(entry.trim());
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
