package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.TRAVEL)
public final class TravelNode extends NavigationNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().startNavigation(invocation.args(), true);
        return 0;
    }
}
