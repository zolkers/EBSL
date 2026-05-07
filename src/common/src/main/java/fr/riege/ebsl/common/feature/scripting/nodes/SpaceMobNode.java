package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.task.MobTargetMode;
import fr.riege.ebsl.common.feature.task.SpaceMobTask;

@EbslNodeDefinition(EbslNodeType.SPACE_MOB)
public final class SpaceMobNode extends AbstractEbslNode {

    @Override
    public void finish(EbslNodeInvocation invocation) {
        if (hasDuration(invocation)) {
            SpaceMobTask.INSTANCE.setEnabled(false);
            SpaceMobTask.INSTANCE.onDisable();
        }
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (isStop(invocation)) {
            SpaceMobTask.INSTANCE.setEnabled(false);
            SpaceMobTask.INSTANCE.onDisable();
            return 0;
        }
        configure(invocation);
        SpaceMobTask.INSTANCE.setEnabled(true);
        return duration(invocation);
    }

    private boolean isStop(EbslNodeInvocation invocation) {
        if (invocation.args().isEmpty()) {
            return false;
        }
        SpaceMobDirective directive = SpaceMobDirective.byToken(invocation.arg(0));
        return directive == SpaceMobDirective.OFF || directive == SpaceMobDirective.STOP;
    }

    private void configure(EbslNodeInvocation invocation) {
        Cursor cursor = new Cursor(invocation);
        if (cursor.peek(SpaceMobDirective.ON)) {
            cursor.next();
        }
        MobTargetMode mode = MobTargetMode.CLOSEST_MOB;
        String name = "";
        if (cursor.consume(SpaceMobDirective.NAME)) {
            mode = MobTargetMode.ENTITY_NAME;
            name = cursor.hasNext() ? cursor.next() : "";
        }
        if (name.isBlank()) {
            cursor.consume(SpaceMobDirective.CLOSEST);
        }
        double distance = cursor.nextNumber(3.0);
        double tolerance = cursor.nextNumber(0.35);
        int radius = (int) cursor.nextNumber(32.0);
        boolean track = cursor.consume(SpaceMobDirective.TRACK);
        SpaceMobTask.INSTANCE.configure(mode, name, distance, tolerance, radius, track);
    }

    private int duration(EbslNodeInvocation invocation) {
        if (!hasDuration(invocation)) {
            return 0;
        }
        return EbslDuration.ticks(invocation.args().get(invocation.args().size() - 1));
    }

    private boolean hasDuration(EbslNodeInvocation invocation) {
        if (invocation.args().isEmpty()) {
            return false;
        }
        String last = invocation.args().get(invocation.args().size() - 1);
        return last.endsWith("t") || last.endsWith("s") || last.endsWith("ms");
    }

    private static final class Cursor {
        private final EbslNodeInvocation invocation;
        private int index;

        private Cursor(EbslNodeInvocation invocation) {
            this.invocation = invocation;
        }

        private boolean hasNext() {
            return index < invocation.args().size();
        }

        private boolean peek(SpaceMobDirective directive) {
            return hasNext() && SpaceMobDirective.byToken(invocation.arg(index)) == directive;
        }

        private String next() {
            return invocation.arg(index++);
        }

        private boolean consume(SpaceMobDirective directive) {
            if (!peek(directive)) {
                return false;
            }
            index++;
            return true;
        }

        private double nextNumber(double fallback) {
            if (!hasNext()) {
                return fallback;
            }
            String token = invocation.arg(index);
            if (token.endsWith("t") || token.endsWith("s") || token.endsWith("ms")
                || SpaceMobDirective.byToken(token) == SpaceMobDirective.TRACK) {
                return fallback;
            }
            index++;
            return invocation.runtime().number(invocation.runtime().value(token));
        }
    }
}
