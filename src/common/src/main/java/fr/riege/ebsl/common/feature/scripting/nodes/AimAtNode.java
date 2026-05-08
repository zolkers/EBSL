package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.feature.aim.AimProcessor;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.math.Vec3d;

@EbslNodeDefinition(value = EbslNodeType.AIM_AT, aliases = {"aim"})
public final class AimAtNode extends AbstractEbslNode {
    private final AimProcessor aimProcessor = new AimProcessor();

    @Override
    protected void registerSettings() {
        registerSetting(new IntSetting("x", "X", 0, -30000000, 30000000));
        registerSetting(new IntSetting("y", "Y", 64, -64, 512));
        registerSetting(new IntSetting("z", "Z", 0, -30000000, 30000000));
        registerSetting(new IntSetting("ticks", "Ticks", 8, 1, 200));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        aimProcessor.reset();
        tick(invocation);
        return duration(invocation);
    }

    @Override
    public void tick(EbslNodeInvocation invocation) {
        if (invocation.args().size() < 3) {
            return;
        }
        aimProcessor.aimAt(invocation.runtime().platform(), target(invocation));
    }

    @Override
    public void finish(EbslNodeInvocation invocation) {
        aimProcessor.reset();
    }

    private Vec3d target(EbslNodeInvocation invocation) {
        double x = invocation.runtime().number(invocation.runtime().value(invocation.arg(0)));
        double y = invocation.runtime().number(invocation.runtime().value(invocation.arg(1)));
        double z = invocation.runtime().number(invocation.runtime().value(invocation.arg(2)));
        return new Vec3d(x, y, z);
    }

    private int duration(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 4) {
            return EbslDuration.ticks(invocation.arg(3));
        }
        return 8;
    }
}
