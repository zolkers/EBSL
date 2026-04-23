package fr.riege.ebsl.pathfinding;

public class PathfinderConfig {

    public static final Value<Integer> PATHFINDER_MAX_JUMP_HEIGHT = new Value<>(1);
    public static final Value<Boolean> SHOW_DEBUG = new Value<>(true);

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
