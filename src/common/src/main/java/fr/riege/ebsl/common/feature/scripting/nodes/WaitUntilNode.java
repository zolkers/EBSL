package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import java.util.List;

@EbslNodeDefinition(value = EbslNodeType.CONTROL_WAIT_UNTIL, aliases = {"wait_until"})
public final class WaitUntilNode extends AbstractEbslNode {
    private final StringSetting condition = registerSetting(new StringSetting("condition", "Condition", "true"));

    @Override
    public boolean isWaitUntil() {
        return true;
    }

    @Override
    public void loadArgs(List<String> args) {
        condition.setValue(args.isEmpty() ? condition.defaultValue() : String.join(" ", args));
    }

    @Override
    public String argsFromSettings() {
        return condition.value().trim();
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        return 0;
    }
}
