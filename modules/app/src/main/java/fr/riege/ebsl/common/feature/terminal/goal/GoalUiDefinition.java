package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.*;

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

    public String id() { return id; }
    public String label() { return label; }
    public String description() { return description; }
    public List<GoalParameter> parameters() { return parameters; }

    public int execute(NavigationService navigation, Map<String, Integer> values) {
        return executor.execute(navigation, Collections.unmodifiableMap(new LinkedHashMap<>(values)));
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

        public Builder description(String d) { description = d; return this; }
        public Builder parameter(GoalParameter p) { parameters.add(p); return this; }
        public Builder executor(GoalUiExecutor e) { executor = e; return this; }

        public Builder currentXYZ() {
            return parameter(GoalParameter.currentX())
                .parameter(GoalParameter.currentY())
                .parameter(GoalParameter.currentZ());
        }

        public GoalUiDefinition build() {
            if (executor == null) throw new IllegalStateException("Goal " + id + " has no executor");
            return new GoalUiDefinition(id, label, description, parameters, executor);
        }
    }
}
