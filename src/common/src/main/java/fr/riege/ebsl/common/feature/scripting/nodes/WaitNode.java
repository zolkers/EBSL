package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.WAIT)
public final class WaitNode extends AbstractEbslNode {
    private final StringSetting duration = registerSetting(new StringSetting("duration", "Duration", "1t"));

    @Override
    public int start(EbslNodeInvocation invocation) {
        return EbslDuration.ticks(invocation.args().isEmpty() ? "1t" : invocation.arg(0));
    }
}
