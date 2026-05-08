package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;

@EbslNodeDefinition(EbslNodeType.PRESS_KEY)
public final class PressKeyNode extends AbstractEbslNode {
    private final EnumSetting<EbslInputKey> key = registerSetting(new EnumSetting<>("key", "Key", EbslInputKey.JUMP, EbslInputKey.class));
    private final StringSetting duration = registerSetting(new StringSetting("duration", "Duration", "4t"));

    @Override
    public boolean releasesGameplayKeys() {
        return true;
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().isEmpty()) {
            return 0;
        }
        TimedInputNode.press(invocation.runtime(), invocation.arg(0));
        return EbslDuration.ticks(invocation.args().size() >= 2 ? invocation.arg(1) : "4t");
    }
}
