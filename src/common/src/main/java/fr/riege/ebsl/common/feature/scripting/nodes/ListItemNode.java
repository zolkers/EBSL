package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import java.util.List;

@EbslNodeDefinition(EbslNodeType.LIST_ITEM)
public final class ListItemNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 3) {
            List<Object> list = invocation.runtime().list(invocation.arg(1));
            int index = (int) invocation.runtime().number(invocation.runtime().value(invocation.arg(2)));
            invocation.runtime().setVariable(invocation.arg(0), index >= 0 && index < list.size() ? list.get(index) : "");
        }
        return 0;
    }
}
