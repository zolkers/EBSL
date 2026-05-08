package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

import java.util.List;
import java.util.Locale;

@EbslNodeDefinition(EbslNodeType.BREAK_BLOCK)
public final class BreakBlockNode extends AbstractEbslNode {
    private static final String DEFAULT_MAX_DURATION = "5s";

    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("block", "Block", ""));
        registerSetting(new StringSetting("max_duration", "Max Duration", DEFAULT_MAX_DURATION));
    }

    @Override
    public boolean releasesGameplayKeys() {
        return true;
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        TimedInputNode.press(invocation.runtime(), EbslInputKey.ATTACK);
        return EbslDuration.ticks(maxDuration(invocation.args()));
    }

    @Override
    public boolean isComplete(EbslNodeInvocation invocation) {
        BlockId target = invocation.runtime().platform().player().targetedBlock();
        if (target == null) {
            return true;
        }
        String expected = expectedBlock(invocation.args());
        return !expected.isBlank() && !target.toString().equalsIgnoreCase(expected);
    }

    private static String expectedBlock(List<String> args) {
        if (args.isEmpty() || looksLikeDuration(args.getFirst())) {
            return "";
        }
        return args.getFirst().trim();
    }

    private static String maxDuration(List<String> args) {
        if (args.isEmpty()) {
            return DEFAULT_MAX_DURATION;
        }
        if (looksLikeDuration(args.getFirst())) {
            return args.getFirst();
        }
        return args.size() > 1 ? args.get(1) : DEFAULT_MAX_DURATION;
    }

    private static boolean looksLikeDuration(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.endsWith("t") || normalized.endsWith("s");
    }
}
