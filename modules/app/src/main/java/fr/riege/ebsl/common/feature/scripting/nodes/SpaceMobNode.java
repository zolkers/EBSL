package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.DoubleSetting;
import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslDurationUnit;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.task.MobTargetMode;
import fr.riege.ebsl.common.feature.task.SpaceMobTask;

@EbslNodeDefinition(EbslNodeType.SPACE_MOB)
public final class SpaceMobNode extends AbstractEbslNode {
    private static final String CLOSEST_TARGET = "closest";

    private StringSetting target;
    private DoubleSetting distance;
    private DoubleSetting tolerance;
    private IntSetting radius;
    private BooleanSetting track;
    private StringSetting duration;

    @Override
    protected void registerSettings() {
        target = registerSetting(new StringSetting("target", "Target", CLOSEST_TARGET));
        distance = registerSetting(new DoubleSetting("distance", "Distance", 3.0, 0.1, 12.0));
        tolerance = registerSetting(new DoubleSetting("tolerance", "Tolerance", 0.35, 0.05, 4.0));
        radius = registerSetting(new IntSetting("radius", "Radius", 32, 1, 128));
        track = registerSetting(new BooleanSetting("track", "Track", true));
        duration = registerSetting(new StringSetting("duration", "Duration", ""));
    }

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

    @Override
    public void loadArgs(java.util.List<String> args) {
        settings();
        Cursor cursor = new Cursor(new EbslNodeInvocation(args, null, null));
        if (cursor.peek(SpaceMobDirective.ON)) {
            cursor.next();
        }
        target.setValue(cursor.consume(SpaceMobDirective.NAME) && cursor.hasNext() ? cursor.next() : CLOSEST_TARGET);
        cursor.consume(SpaceMobDirective.CLOSEST);
        distance.setValue(cursor.nextRawNumber(3.0));
        tolerance.setValue(cursor.nextRawNumber(0.35));
        radius.setValue((int) cursor.nextRawNumber(32.0));
        track.setValue(cursor.consume(SpaceMobDirective.TRACK));
        duration.setValue(cursor.hasNext() ? cursor.next() : "");
    }

    @Override
    public String argsFromSettings() {
        settings();
        StringBuilder builder = new StringBuilder("on ");
        if (target.value().isBlank() || CLOSEST_TARGET.equalsIgnoreCase(target.value())) {
            builder.append(CLOSEST_TARGET);
        } else {
            builder.append("name ").append(target.value());
        }
        builder.append(' ').append(distance.value())
            .append(' ').append(tolerance.value())
            .append(' ').append(radius.value());
        if (Boolean.TRUE.equals(track.value())) {
            builder.append(" track");
        }
        if (!duration.value().isBlank()) {
            builder.append(' ').append(duration.value().trim());
        }
        return builder.toString();
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
        double targetDistance = cursor.nextNumber(3.0);
        double targetTolerance = cursor.nextNumber(0.35);
        int searchRadius = (int) cursor.nextNumber(32.0);
        boolean trackTarget = cursor.consume(SpaceMobDirective.TRACK);
        SpaceMobTask.INSTANCE.configure(mode, name, targetDistance, targetTolerance, searchRadius, trackTarget);
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
        return EbslDurationUnit.hasDurationSuffix(last);
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
            if (EbslDurationUnit.hasDurationSuffix(token)
                || SpaceMobDirective.byToken(token) == SpaceMobDirective.TRACK) {
                return fallback;
            }
            index++;
            return invocation.runtime().number(invocation.runtime().value(token));
        }

        private double nextRawNumber(double fallback) {
            if (!hasNext()) {
                return fallback;
            }
            String token = invocation.arg(index);
            if (EbslDurationUnit.hasDurationSuffix(token)
                || SpaceMobDirective.byToken(token) == SpaceMobDirective.TRACK) {
                return fallback;
            }
            index++;
            try {
                return Double.parseDouble(token);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
    }
}
