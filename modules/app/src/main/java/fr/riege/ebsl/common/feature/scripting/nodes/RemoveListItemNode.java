package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

import java.util.List;

@EbslNodeDefinition(value = EbslNodeType.REMOVE_LIST_ITEM, aliases = {"remove_from_list"})
public final class RemoveListItemNode extends AbstractEbslNode {
    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("list", "List", "items"));
        registerSetting(new StringSetting("index", "Index", "0"));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 2) {
            List<Object> list = invocation.runtime().list(invocation.arg(0));
            int index = (int) invocation.runtime().number(invocation.runtime().value(invocation.arg(1)));
            if (index >= 0 && index < list.size()) {
                list.remove(index);
            }
        }
        return 0;
    }
}
