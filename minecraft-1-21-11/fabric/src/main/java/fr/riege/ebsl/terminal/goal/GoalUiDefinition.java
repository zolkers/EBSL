package fr.riege.ebsl.terminal.goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GoalUiDefinition {
    private final String id;
    private final String label;
    private final String description;
    private final List<GoalParameter> parameters;
    private final GoalUiExecutor executor;

    private GoalUiDefinition(String id, String label, String description,
                             List<GoalParameter> parameters, GoalUiExecutor executor) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.parameters = List.copyOf(parameters);
        this.executor = executor;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public List<GoalParameter> parameters() {
        return parameters;
    }

    public int execute(Map<String, Integer> values) {
        return executor.execute(Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }

    public static Builder builder(String id, String label) {
        return new Builder(id, label);
    }

    public static final class Builder {
        private final String id;
        private final String label;
        private final List<GoalParameter> parameters = new ArrayList<>();
        private String description = "";
        private GoalUiExecutor executor;

        private Builder(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder parameter(GoalParameter parameter) {
            parameters.add(parameter);
            return this;
        }

        public Builder currentXYZ() {
            return parameter(GoalParameter.currentX())
                .parameter(GoalParameter.currentY())
                .parameter(GoalParameter.currentZ());
        }

        public Builder executor(GoalUiExecutor executor) {
            this.executor = executor;
            return this;
        }

        public GoalUiDefinition build() {
            if (executor == null) {
                throw new IllegalStateException("Goal UI definition " + id + " has no executor");
            }
            return new GoalUiDefinition(id, label, description, parameters, executor);
        }
    }
}
