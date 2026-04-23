package fr.riege.ebsl.pathfinding.check;

final class MissedCheckpointCheck implements PathCheck {
    private static final int MAX_SKIPPED_CHECKPOINTS = 5;
    private static final double MAX_NEXT_CHECKPOINT_DISTANCE = 6.0;
    private static final long MAX_CHECKPOINT_STALE_MS = 3000L;

    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathCheckpointSnapshot checkpoint = context.checkpoint();
        if (checkpoint.skippedCheckpoints() >= MAX_SKIPPED_CHECKPOINTS
            && checkpoint.distanceToNextCheckpoint() >= MAX_NEXT_CHECKPOINT_DISTANCE
            && checkpoint.staleDurationMs() >= MAX_CHECKPOINT_STALE_MS) {
            return PathCheckResult.forceReplan(String.format(
                "missed checkpoints checkpoint=%d projected=%d skipped=%d dist=%.2f stale=%dms",
                checkpoint.checkpointIndex(),
                checkpoint.projectedSegmentIndex(),
                checkpoint.skippedCheckpoints(),
                checkpoint.distanceToNextCheckpoint(),
                checkpoint.staleDurationMs()));
        }
        return PathCheckResult.none();
    }
}
