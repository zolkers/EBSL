package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.GOAL)
public final class GoalNode extends NavigationNode {
    private final IntSetting x = registerSetting(new IntSetting("x", "X", 0, -30000000, 30000000));
    private final IntSetting y = registerSetting(new IntSetting("y", "Y", 64, -64, 512));
    private final IntSetting z = registerSetting(new IntSetting("z", "Z", 0, -30000000, 30000000));

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().startNavigation(invocation.args(), false);
        return 0;
    }
}
