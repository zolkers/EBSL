package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.OPERATOR_MOD)
public final class OperatorModNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 3) {
            double right = invocation.runtime().number(invocation.runtime().value(invocation.arg(2)));
            double result = right == 0.0 ? 0.0 : invocation.runtime().number(invocation.runtime().value(invocation.arg(1))) % right;
            invocation.runtime().setVariable(invocation.arg(0), result);
        }
        return 0;
    }
}
