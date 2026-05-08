package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.terminal.goal.GoalParameter;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CatalogGoalNode extends NavigationNode {
    private static final String PREFIX = "goal_";
    private final GoalUiDefinition definition;

    public CatalogGoalNode(GoalUiDefinition definition) {
        super(PREFIX + definition.id());
        this.definition = definition;
        for (GoalParameter parameter : definition.parameters()) {
            registerSetting(new IntSetting(parameter.id(), parameter.label(), 0, -30000000, 30000000));
        }
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (int i = 0; i < definition.parameters().size(); i++) {
            GoalParameter parameter = definition.parameters().get(i);
            values.put(parameter.id(), integerArg(invocation, i));
        }
        definition.execute(invocation.runtime().navigation(), values);
        return 0;
    }

    private int integerArg(EbslNodeInvocation invocation, int index) {
        if (index >= invocation.args().size()) {
            return 0;
        }
        return (int) Math.floor(invocation.runtime().number(invocation.runtime().value(invocation.arg(index))));
    }
}
