package fr.riege.ebsl.common.terminal.goal;

public record GoalParameter(String label, String defaultValue) {
    public static GoalParameter of(String label, String defaultValue) {
        return new GoalParameter(label, defaultValue);
    }
}
