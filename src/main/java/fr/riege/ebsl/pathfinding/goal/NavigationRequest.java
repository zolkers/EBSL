package fr.riege.ebsl.pathfinding.goal;

import net.minecraft.world.entity.Entity;

public record NavigationRequest(
    Goal goal,
    NavigationModeType mode,
    boolean allowReplan,
    double preciseGoalTolerance,
    Runnable onFinished,
    Runnable onFailed,
    Entity rotationTarget
) {
    public static Builder builder(Goal goal) {
        return new Builder(goal);
    }

    public boolean isPrecise() {
        return preciseGoalTolerance != 0.5;
    }

    public static final class Builder {
        private final Goal goal;
        private NavigationModeType mode = NavigationModeType.WALK;
        private boolean allowReplan = true;
        private double preciseGoalTolerance = 0.5;
        private Runnable onFinished;
        private Runnable onFailed;
        private Entity rotationTarget;

        private Builder(Goal goal) {
            this.goal = goal;
        }

        public Builder mode(NavigationModeType mode) {
            this.mode = mode;
            return this;
        }

        public Builder allowReplan(boolean allowReplan) {
            this.allowReplan = allowReplan;
            return this;
        }

        public Builder preciseGoalTolerance(double preciseGoalTolerance) {
            this.preciseGoalTolerance = preciseGoalTolerance;
            return this;
        }

        public Builder onFinished(Runnable onFinished) {
            this.onFinished = onFinished;
            return this;
        }

        public Builder onFailed(Runnable onFailed) {
            this.onFailed = onFailed;
            return this;
        }

        public Builder rotationTarget(Entity rotationTarget) {
            this.rotationTarget = rotationTarget;
            return this;
        }

        public NavigationRequest build() {
            return new NavigationRequest(
                goal,
                mode,
                allowReplan,
                preciseGoalTolerance,
                onFinished,
                onFailed,
                rotationTarget
            );
        }
    }
}
