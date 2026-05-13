package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

@EbslNodeDefinition(EbslNodeType.WALK)
public final class WalkNode extends AbstractEbslNode {
    @Override
    protected void registerSettings() {
        registerSetting(new EnumSetting<>("key", "Key", EbslInputKey.FORWARD, EbslInputKey.class));
        registerSetting(new StringSetting("duration", "Duration", "20t"));
    }

    @Override
    public boolean releasesGameplayKeys() {
        return true;
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        TimedInputNode.press(invocation.runtime(), invocation.args().isEmpty() ? "forward" : invocation.arg(0));
        return EbslDuration.ticks(invocation.args().size() >= 2 ? invocation.arg(1) : "20t");
    }
}
