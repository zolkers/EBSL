package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.GOAL)
public final class GoalNode extends NavigationNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().startNavigation(invocation.args(), false);
        return 0;
    }
}
