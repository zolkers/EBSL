package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(value = EbslNodeType.CHANGE_VARIABLE, aliases = {"change"})
public final class ChangeVariableNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 2) {
            double value = invocation.runtime().number(invocation.runtime().variable(invocation.arg(0)))
                + invocation.runtime().number(invocation.runtime().value(invocation.arg(1)));
            invocation.runtime().setVariable(invocation.arg(0), value);
        }
        return 0;
    }
}
