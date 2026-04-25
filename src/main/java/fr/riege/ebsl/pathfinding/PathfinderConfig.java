package fr.riege.ebsl.pathfinding;

public class PathfinderConfig {

    public static final Value<Integer> PATHFINDER_MAX_JUMP_HEIGHT = new Value<>(1);
    public static final Value<Boolean> SHOW_DEBUG = new Value<>(true);
    public static final Value<Double> WALK_COST = new Value<>(0.0);
    public static final Value<Double> DIAGONAL_COST = new Value<>(0.0);
    public static final Value<Double> FULL_STEP_ASCENT_BASE_COST = new Value<>(2.0);
    public static final Value<Double> FULL_STEP_ASCENT_DY_COST = new Value<>(0.5);
    public static final Value<Double> PARTIAL_ASCENT_COST = new Value<>(0.0);
    public static final Value<Double> JUMP_COST = new Value<>(0.0);
    public static final Value<Double> PARKOUR_COST = new Value<>(0.0);
    public static final Value<Double> FALL_DY_COST = new Value<>(0.1);
    public static final Value<Double> SWIM_COST = new Value<>(0.0);
    public static final Value<Double> CLIMB_COST = new Value<>(0.0);
    public static final Value<Double> CARDINAL_WALL_COST = new Value<>(0.55);
    public static final Value<Double> DIAGONAL_WALL_COST = new Value<>(0.25);
    public static final Value<Double> PARTIAL_ASCENT_EDGE_COST = new Value<>(0.32);
    public static final Value<Double> PARTIAL_ASCENT_ENTRY_SIDE_COST = new Value<>(0.28);
    public static final Value<Double> OPENING_ENTRY_IMBALANCE_COST = new Value<>(0.24);

    public static class Value<T> {
        private T value;

        public Value(T defaultValue) {
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}
