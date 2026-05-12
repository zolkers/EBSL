package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.TRAVEL)
public final class TravelNode extends NavigationNode {
    @Override
    protected void registerSettings() {
        registerSetting(new IntSetting("x", "X", 0, -30000000, 30000000));
        registerSetting(new IntSetting("y", "Y", 64, -64, 512));
        registerSetting(new IntSetting("z", "Z", 0, -30000000, 30000000));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().startNavigation(invocation.args(), true);
        return 0;
    }
}
