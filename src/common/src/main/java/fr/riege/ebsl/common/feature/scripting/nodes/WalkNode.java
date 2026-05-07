package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.WALK)
public final class WalkNode extends AbstractEbslNode {

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
