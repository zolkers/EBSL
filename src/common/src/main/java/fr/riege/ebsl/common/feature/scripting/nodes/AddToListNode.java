package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.ADD_TO_LIST)
public final class AddToListNode extends AbstractEbslNode {
    private final StringSetting list = registerSetting(new StringSetting("list", "List", "items"));
    private final StringSetting value = registerSetting(new StringSetting("value", "Value", "value"));

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 2) {
            invocation.runtime().list(invocation.arg(0)).add(invocation.runtime().value(invocation.arg(1)));
        }
        return 0;
    }
}
