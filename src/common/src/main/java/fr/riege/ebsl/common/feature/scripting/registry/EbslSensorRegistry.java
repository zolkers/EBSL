package fr.riege.ebsl.common.feature.scripting.registry;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.scripting.enums.EbslCardinalDirection;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
import fr.riege.ebsl.common.math.Vec3d;

import java.util.List;

public final class EbslSensorRegistry {
    private static final MapRegistry<String, Sensor> SENSORS = new MapRegistry<>(null);

    static {
        register("sensor_health_below", (runtime, args) -> runtime.platform().player().getHealth() < runtime.argNumber(args, 0, 10.0));
        register("sensor_is_swimming", (runtime, args) -> runtime.platform().player().isInWater());
        register("sensor_is_underwater", (runtime, args) -> runtime.platform().player().isInWater());
        register("sensor_is_in_lava", (runtime, args) -> runtime.platform().player().isInLava());
        register("sensor_is_on_ground", (runtime, args) -> runtime.platform().player().onGround());
        register("sensor_is_falling", (runtime, args) -> runtime.platform().player().velocity().y() < -runtime.argNumber(args, 0, 0.08));
        register("sensor_at_coordinates", EbslSensorRegistry::atCoordinates);
        register("sensor_distance_between", EbslSensorRegistry::distanceBetween);
        register("sensor_targeted_block", EbslSensorRegistry::targetedBlock);
        register("sensor_look_direction", EbslSensorRegistry::lookDirection);
        register("sensor_touching_entity", EbslSensorRegistry::touchingEntity);
        register("sensor_is_rendered", (runtime, args) -> !runtime.platform().entities().entitiesForRendering().isEmpty());
        register("sensor_is_visible", (runtime, args) -> !runtime.platform().entities().entitiesForRendering().isEmpty());
        register("sensor_key_pressed", (runtime, args) -> keyPressed(runtime, args.isEmpty() ? "" : args.get(0)));
    }

    private EbslSensorRegistry() {
    }

    public static boolean evaluate(String id, EbslScriptRuntime runtime, List<String> args) {
        Sensor sensor = SENSORS.get(id);
        return sensor != null && sensor.evaluate(runtime, args);
    }

    private static void register(String id, Sensor sensor) {
        SENSORS.register(id, sensor);
    }

    private static boolean atCoordinates(EbslScriptRuntime runtime, List<String> args) {
        if (args.size() < 3) {
            return false;
        }
        Vec3d pos = runtime.platform().player().position();
        double tolerance = runtime.argNumber(args, 3, 0.75);
        double dx = pos.x() - runtime.number(runtime.value(args.get(0)));
        double dy = pos.y() - runtime.number(runtime.value(args.get(1)));
        double dz = pos.z() - runtime.number(runtime.value(args.get(2)));
        return dx * dx + dy * dy + dz * dz <= tolerance * tolerance;
    }

    private static boolean distanceBetween(EbslScriptRuntime runtime, List<String> args) {
        if (args.size() < 4) {
            return false;
        }
        Vec3d pos = runtime.platform().player().position();
        double dx = pos.x() - runtime.number(runtime.value(args.get(0)));
        double dy = pos.y() - runtime.number(runtime.value(args.get(1)));
        double dz = pos.z() - runtime.number(runtime.value(args.get(2)));
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= runtime.number(runtime.value(args.get(3)));
    }

    private static boolean targetedBlock(EbslScriptRuntime runtime, List<String> args) {
        var target = runtime.platform().player().targetedBlock();
        return target != null && (args.isEmpty() || target.toString().equalsIgnoreCase(args.get(0)));
    }

    private static boolean touchingEntity(EbslScriptRuntime runtime, List<String> args) {
        double radius = runtime.argNumber(args, 0, 2.0);
        double radiusSq = radius * radius;
        Vec3d playerPos = runtime.platform().player().position();
        return runtime.platform().entities().entitiesForRendering().stream()
            .anyMatch(entity -> entity.distanceToSq(playerPos) <= radiusSq);
    }

    private static boolean lookDirection(EbslScriptRuntime runtime, List<String> args) {
        if (args.isEmpty()) {
            return true;
        }
        EbslCardinalDirection direction = EbslCardinalDirection.byToken(args.get(0));
        return direction != null && direction.matches(runtime.platform().player().yaw());
    }

    private static boolean keyPressed(EbslScriptRuntime runtime, String key) {
        EbslInputKey inputKey = EbslInputKey.byToken(key);
        return inputKey != null && inputKey.isDown(runtime.platform().input());
    }

    @FunctionalInterface
    private interface Sensor {
        boolean evaluate(EbslScriptRuntime runtime, List<String> args);
    }
}
