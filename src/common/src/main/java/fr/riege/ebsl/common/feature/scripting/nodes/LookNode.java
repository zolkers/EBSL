package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.LOOK)
public final class LookNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 2) {
            float yaw = (float) invocation.runtime().number(invocation.runtime().value(invocation.arg(0)));
            float pitch = (float) invocation.runtime().number(invocation.runtime().value(invocation.arg(1)));
            invocation.runtime().platform().input().lookAt(yaw, pitch);
        }
        return 0;
    }
}
