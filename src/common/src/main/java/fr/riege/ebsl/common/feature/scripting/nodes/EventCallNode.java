package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslStatement;
import java.util.List;

@EbslNodeDefinition(EbslNodeType.EVENT_CALL)
public final class EventCallNode extends AbstractEbslNode {

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (!invocation.args().isEmpty()) {
            List<EbslStatement> function = invocation.runner().function(invocation.arg(0));
            if (function != null) {
                invocation.runner().call(function);
            }
        }
        return 0;
    }
}
