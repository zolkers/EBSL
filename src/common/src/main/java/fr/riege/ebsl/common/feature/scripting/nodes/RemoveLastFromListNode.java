package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import java.util.List;

@EbslNodeDefinition(EbslNodeType.REMOVE_LAST_FROM_LIST)
public final class RemoveLastFromListNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (!invocation.args().isEmpty() && !invocation.runtime().list(invocation.arg(0)).isEmpty()) {
            List<Object> list = invocation.runtime().list(invocation.arg(0));
            list.remove(list.size() - 1);
        }
        return 0;
    }
}
