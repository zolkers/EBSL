package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;

@EbslNodeDefinition(EbslNodeType.OPERATOR_RANDOM)
public final class OperatorRandomNode extends AbstractEbslNode {
    private final StringSetting variable = registerSetting(new StringSetting("variable", "Variable", "random"));
    private final StringSetting min = registerSetting(new StringSetting("min", "Min", "0"));
    private final StringSetting max = registerSetting(new StringSetting("max", "Max", "1"));

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 3) {
            double min = invocation.runtime().number(invocation.runtime().value(invocation.arg(1)));
            double max = invocation.runtime().number(invocation.runtime().value(invocation.arg(2)));
            invocation.runtime().setVariable(invocation.arg(0), invocation.runtime().random(min, max));
        }
        return 0;
    }
}
