package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

@EbslNodeDefinition(EbslNodeType.START_CHAIN)
public final class StartChainNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        return 0;
    }
}
