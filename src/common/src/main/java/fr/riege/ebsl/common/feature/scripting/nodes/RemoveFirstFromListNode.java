package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.REMOVE_FIRST_FROM_LIST)
public final class RemoveFirstFromListNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (!invocation.args().isEmpty() && !invocation.runtime().list(invocation.arg(0)).isEmpty()) {
            invocation.runtime().list(invocation.arg(0)).remove(0);
        }
        return 0;
    }
}
