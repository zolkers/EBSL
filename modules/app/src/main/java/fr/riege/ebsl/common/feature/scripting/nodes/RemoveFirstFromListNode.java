package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

@EbslNodeDefinition(EbslNodeType.REMOVE_FIRST_FROM_LIST)
public final class RemoveFirstFromListNode extends AbstractEbslNode {
    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("list", "List", "items"));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (!invocation.args().isEmpty() && !invocation.runtime().list(invocation.arg(0)).isEmpty()) {
            invocation.runtime().list(invocation.arg(0)).removeFirst();
        }
        return 0;
    }
}
