package fr.riege.ebsl.pathfinding.goal;

import java.util.Objects;

public record NavigationRequest(
    Goal goal,
    NavigationModeType mode,
    boolean allowReplan,
    boolean allowParkour,
    boolean allowRotation,
    boolean allowSneak,
    double preciseGoalTolerance,
    Runnable onFinished,
    Runnable onFailed
) {
    public NavigationRequest {
        goal = Objects.requireNonNull(goal, "goal");
        mode = Objects.requireNonNull(mode, "mode");
        if (!Double.isFinite(preciseGoalTolerance) || preciseGoalTolerance < 0.0) {
            throw new IllegalArgumentException("preciseGoalTolerance must be finite and non-negative");
        }
    }

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
        private boolean allowParkour = true;
        private boolean allowRotation = true;
        private boolean allowSneak = true;
        private double preciseGoalTolerance = 0.5;
        private Runnable onFinished;
        private Runnable onFailed;

        private Builder(Goal goal) {
            this.goal = Objects.requireNonNull(goal, "goal");
        }

        public Builder mode(NavigationModeType mode) {
            this.mode = Objects.requireNonNull(mode, "mode");
            return this;
        }

        public Builder allowReplan(boolean allowReplan) {
            this.allowReplan = allowReplan;
            return this;
        }

        public Builder allowParkour(boolean allowParkour) {
            this.allowParkour = allowParkour;
            return this;
        }

        public Builder allowRotation(boolean allowRotation) {
            this.allowRotation = allowRotation;
            return this;
        }

        public Builder allowSneak(boolean allowSneak) {
            this.allowSneak = allowSneak;
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

        public NavigationRequest build() {
            return new NavigationRequest(
                goal,
                mode,
                allowReplan,
                allowParkour,
                allowRotation,
                allowSneak,
                preciseGoalTolerance,
                onFinished,
                onFailed
            );
        }
    }
}
