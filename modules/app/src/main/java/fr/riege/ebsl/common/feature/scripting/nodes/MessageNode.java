package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

@EbslNodeDefinition(EbslNodeType.MESSAGE)
public final class MessageNode extends AbstractEbslNode {
    private StringSetting text;

    @Override
    protected void registerSettings() {
        text = registerSetting(new StringSetting("text", "Text", "hello"));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().platform().commands().printSuccess(String.join(" ", invocation.args()));
        return 0;
    }

    @Override
    public void loadArgs(java.util.List<String> args) {
        settings();
        text.setValue(String.join(" ", args));
    }
}
