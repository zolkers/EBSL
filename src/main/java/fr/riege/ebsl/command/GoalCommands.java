package fr.riege.ebsl.command;

public final class GoalCommands {
    private GoalCommands() {
    }

    public static void bootstrap() {
        if (!GoalRegistry.isEmpty()) {
            return;
        }

        NavigationGoalCommands.register();
        AreaGoalCommands.register();
    }
}
