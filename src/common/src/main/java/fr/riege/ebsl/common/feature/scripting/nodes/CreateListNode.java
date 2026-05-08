package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.CREATE_LIST)
public final class CreateListNode extends AbstractEbslNode {
    private final StringSetting list = registerSetting(new StringSetting("list", "List", "items"));

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (!invocation.args().isEmpty()) {
            invocation.runtime().list(invocation.arg(0)).clear();
        }
        return 0;
    }
}
