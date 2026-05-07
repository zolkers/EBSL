package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.MESSAGE)
public final class MessageNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().platform().commands().printSuccess(String.join(" ", invocation.args()));
        return 0;
    }
}
