package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(value = EbslNodeType.SET_VARIABLE, aliases = {"set"})
public final class SetVariableNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 2) {
            invocation.runtime().setVariable(invocation.arg(0), invocation.runtime().value(invocation.arg(1)));
        }
        return 0;
    }
}
