package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;

abstract class TimedInputNode extends AbstractEbslNode {
    private final EbslInputKey key;
    private final String fallbackDuration;

    TimedInputNode(EbslInputKey key, String fallbackDuration) {
        super();
        this.key = key;
        this.fallbackDuration = fallbackDuration;
    }

    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("duration", "Duration", fallbackDuration));
    }

    @Override
    public final boolean releasesGameplayKeys() {
        return true;
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        press(invocation.runtime(), key);
        return EbslDuration.ticks(invocation.args().isEmpty() ? fallbackDuration : invocation.arg(0));
    }

    static void press(EbslScriptRuntime runtime, EbslInputKey key) {
        key.set(runtime.platform().input(), true);
    }

    static void press(EbslScriptRuntime runtime, String token) {
        EbslInputKey key = EbslInputKey.byToken(token);
        if (key != null) {
            press(runtime, key);
        }
    }
}
