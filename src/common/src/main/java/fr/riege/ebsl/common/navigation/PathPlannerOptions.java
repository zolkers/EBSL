package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

public record PathPlannerOptions(
    int maxIterations,
    int maxLength,
    int maxJumpHeight,
    boolean async,
    boolean fallback,
    boolean allowParkour,
    boolean allowJump,
    boolean allowFall,
    boolean allowWalkDiagonal,
    boolean processPath
) {
    public static PathPlannerOptions defaults() {
        PathfinderSettings settings = PathfinderSettings.instance();
        return builder()
            .maxIterations(settings.defaultWalkMaxIterations.value())
            .maxLength(settings.defaultWalkMaxLength.value())
            .maxJumpHeight(settings.maxJumpHeight.value())
            .build();
    }

    public static PathPlannerOptions instant() {
        PathfinderSettings settings = PathfinderSettings.instance();
        return defaults().toBuilder()
            .maxIterations(settings.instantWalkMaxIterations.value())
            .maxLength(settings.instantWalkMaxLength.value())
            .build();
    }

    public Builder toBuilder() {
        return builder()
            .maxIterations(maxIterations)
            .maxLength(maxLength)
            .maxJumpHeight(maxJumpHeight)
            .async(async)
            .fallback(fallback)
            .allowParkour(allowParkour)
            .allowJump(allowJump)
            .allowFall(allowFall)
            .allowWalkDiagonal(allowWalkDiagonal)
            .processPath(processPath);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int maxIterations = 50000;
        private int maxLength = 5000;
        private int maxJumpHeight = 2;
        private boolean async = true;
        private boolean fallback = true;
        private boolean allowParkour = true;
        private boolean allowJump = true;
        private boolean allowFall = true;
        private boolean allowWalkDiagonal = true;
        private boolean processPath = true;

        public Builder maxIterations(int value) {
            this.maxIterations = value;
            return this;
        }

        public Builder maxLength(int value) {
            this.maxLength = value;
            return this;
        }

        public Builder maxJumpHeight(int value) {
            this.maxJumpHeight = value;
            return this;
        }

        public Builder async(boolean value) {
            this.async = value;
            return this;
        }

        public Builder fallback(boolean value) {
            this.fallback = value;
            return this;
        }

        public Builder allowParkour(boolean value) {
            this.allowParkour = value;
            return this;
        }

        public Builder allowJump(boolean value) {
            this.allowJump = value;
            return this;
        }

        public Builder allowFall(boolean value) {
            this.allowFall = value;
            return this;
        }

        public Builder allowWalkDiagonal(boolean value) {
            this.allowWalkDiagonal = value;
            return this;
        }

        public Builder processPath(boolean value) {
            this.processPath = value;
            return this;
        }

        public PathPlannerOptions build() {
            return new PathPlannerOptions(
                Math.max(1, maxIterations),
                Math.max(1, maxLength),
                Math.max(1, maxJumpHeight),
                async,
                fallback,
                allowParkour,
                allowJump,
                allowFall,
                allowWalkDiagonal,
                processPath);
        }
    }
}
