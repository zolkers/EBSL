package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(value = EbslNodeType.CONTROL_WAIT_UNTIL, aliases = {"wait_until"})
public final class WaitUntilNode extends AbstractEbslNode {

    @Override
    public boolean isWaitUntil() {
        return true;
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        return 0;
    }
}
