package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.START)
public final class StartNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        return 0;
    }
}
