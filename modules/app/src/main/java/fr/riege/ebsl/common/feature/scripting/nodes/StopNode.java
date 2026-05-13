package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

@EbslNodeDefinition(EbslNodeType.STOP)
public final class StopNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().stop();
        return 0;
    }
}
