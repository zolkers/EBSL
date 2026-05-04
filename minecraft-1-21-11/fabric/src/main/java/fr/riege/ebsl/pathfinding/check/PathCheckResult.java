package fr.riege.ebsl.pathfinding.check;

public record PathCheckResult(PathCheckAction action, int cutoffSegmentIndex, String reason) {
    private static final PathCheckResult NONE = new PathCheckResult(PathCheckAction.NONE, -1, "");

    static PathCheckResult none() {
        return NONE;
    }

    static PathCheckResult cutoff(int cutoffSegmentIndex, String reason) {
        return new PathCheckResult(PathCheckAction.CUTOFF, cutoffSegmentIndex, reason);
    }

    static PathCheckResult repairToSegment(int cutoffSegmentIndex, String reason) {
        return new PathCheckResult(PathCheckAction.REPAIR_TO_SEGMENT, cutoffSegmentIndex, reason);
    }

    static PathCheckResult forceReplan(String reason) {
        return new PathCheckResult(PathCheckAction.FORCE_REPLAN, -1, reason);
    }

    public boolean requiresAction() {
        return action != PathCheckAction.NONE;
    }

    public boolean isCutoff() {
        return action == PathCheckAction.CUTOFF;
    }

    public boolean isRepairToSegment() {
        return action == PathCheckAction.REPAIR_TO_SEGMENT;
    }

    public boolean isForceReplan() {
        return action == PathCheckAction.FORCE_REPLAN;
    }
}
