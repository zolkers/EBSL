package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import java.util.List;

public final class EbslCommandStatement implements EbslStatement {
    private final String command;
    private final List<String> args;
    private boolean started;
    private int ticksLeft;

    public EbslCommandStatement(String command, List<String> args) {
        this.command = command;
        this.args = List.copyOf(args);
    }

    @Override
    public EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner) {
        EbslNode node = EbslNodeRegistry.get(command);
        if (node != null && node.isWaitUntil()) {
            return runtime.condition(args) ? EbslStep.DONE : EbslStep.RUNNING;
        }
        if (!started) {
            started = true;
            ticksLeft = node == null ? 0 : node.start(new EbslNodeInvocation(args, runtime, runner));
            if (node == null && command.startsWith("sensor_") && !args.isEmpty()) {
                runtime.setVariable(args.get(0), runtime.sensor(command, args.subList(1, args.size())));
            }
        }
        if (ticksLeft > 0) {
            ticksLeft--;
            return EbslStep.RUNNING;
        }
        if (node != null && node.waitsForNavigation() && runtime.navigation().isNavigating()) {
            return EbslStep.RUNNING;
        }
        if (node != null && node.releasesGameplayKeys()) {
            runtime.platform().input().releaseGameplayKeys();
        }
        if (node != null) {
            node.finish(new EbslNodeInvocation(args, runtime, runner));
        }
        started = false;
        return EbslStep.DONE;
    }
}
