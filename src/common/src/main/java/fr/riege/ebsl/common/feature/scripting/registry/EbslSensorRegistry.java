package fr.riege.ebsl.common.feature.scripting.registry;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.aim.BlockAimTargeting;
import fr.riege.ebsl.common.feature.scripting.enums.EbslCardinalDirection;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
import fr.riege.ebsl.common.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class EbslSensorRegistry {
    private static final MapRegistry<String, RegisteredSensor> SENSORS = new MapRegistry<>(null);

    static {
        register("sensor_health_below", List.of(parameter("threshold", "Threshold", "10")),
            (runtime, args) -> runtime.platform().player().getHealth() < runtime.argNumber(args, 0, 10.0));
        register("sensor_is_swimming", (runtime, args) -> runtime.platform().player().isInWater());
        register("sensor_is_underwater", (runtime, args) -> runtime.platform().player().isInWater());
        register("sensor_is_in_lava", (runtime, args) -> runtime.platform().player().isInLava());
        register("sensor_is_on_ground", (runtime, args) -> runtime.platform().player().onGround());
        register("sensor_is_falling", List.of(parameter("speed", "Speed", "0.08")),
            (runtime, args) -> runtime.platform().player().velocity().y() < -runtime.argNumber(args, 0, 0.08));
        register("sensor_at_coordinates", List.of(
            parameter("x", "X", "0"),
            parameter("y", "Y", "64"),
            parameter("z", "Z", "0"),
            parameter("tolerance", "Tolerance", "0.75")), EbslSensorRegistry::atCoordinates);
        register("sensor_distance_between", List.of(
            parameter("x", "X", "0"),
            parameter("y", "Y", "64"),
            parameter("z", "Z", "0"),
            parameter("distance", "Distance", "3")), EbslSensorRegistry::distanceBetween);
        register("sensor_targeted_block_exists", EbslSensorRegistry::targetedBlockExists);
        register("sensor_targeted_block", List.of(parameter("block", "Block", "")), EbslSensorRegistry::targetedBlock);
        register("sensor_look_direction", List.of(parameter("direction", "Direction", "north")), EbslSensorRegistry::lookDirection);
        register("sensor_touching_entity", List.of(parameter("radius", "Radius", "2")), EbslSensorRegistry::touchingEntity);
        register("sensor_is_rendered", (runtime, args) -> !runtime.platform().entities().entitiesForRendering().isEmpty());
        register("sensor_is_visible", (runtime, args) -> !runtime.platform().entities().entitiesForRendering().isEmpty());
        register("sensor_key_pressed", List.of(parameter("key", "Key", "jump")),
            (runtime, args) -> keyPressed(runtime, args.isEmpty() ? "" : args.get(0)));
    }

    private EbslSensorRegistry() {
    }

    public static boolean evaluate(String id, EbslScriptRuntime runtime, List<String> args) {
        RegisteredSensor sensor = SENSORS.get(id);
        return sensor != null && sensor.evaluator().evaluate(runtime, args);
    }

    public static SensorDefinition definition(String id) {
        RegisteredSensor sensor = SENSORS.get(id);
        return sensor == null ? null : new SensorDefinition(id, sensor.parameters());
    }

    public static Collection<SensorDefinition> definitions() {
        List<SensorDefinition> definitions = new ArrayList<>();
        for (String id : SENSORS.keys()) {
            definitions.add(definition(id));
        }
        return List.copyOf(definitions);
    }

    private static void register(String id, Sensor sensor) {
        register(id, List.of(), sensor);
    }

    private static void register(String id, List<SensorParameter> parameters, Sensor sensor) {
        SENSORS.register(id, new RegisteredSensor(List.copyOf(parameters), sensor));
    }

    private static SensorParameter parameter(String id, String label, String defaultValue) {
        return new SensorParameter(id, label, defaultValue);
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
        var target = runtime.platform().player().targetedBlockHit();
        if (target == null || runtime.platform().world().isAir(target.x(), target.y(), target.z())) {
            return false;
        }
        return args.isEmpty() || BlockAimTargeting.matches(target.block(), runtime.text(args.get(0)));
    }

    private static boolean targetedBlockExists(EbslScriptRuntime runtime, List<String> args) {
        var target = runtime.platform().player().targetedBlockHit();
        return target != null && !runtime.platform().world().isAir(target.x(), target.y(), target.z());
    }

    private static boolean touchingEntity(EbslScriptRuntime runtime, List<String> args) {
        double radius = runtime.argNumber(args, 0, 2.0);
        double radiusSq = radius * radius;
        Vec3d playerPos = runtime.platform().player().position();
        return runtime.platform().entities().livingEntitiesForTargeting().stream()
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

    private record RegisteredSensor(List<SensorParameter> parameters, Sensor evaluator) {
    }

    public record SensorDefinition(String id, List<SensorParameter> parameters) {
        public SensorDefinition {
            parameters = List.copyOf(parameters);
        }
    }

    public record SensorParameter(String id, String label, String defaultValue) {
    }
}
