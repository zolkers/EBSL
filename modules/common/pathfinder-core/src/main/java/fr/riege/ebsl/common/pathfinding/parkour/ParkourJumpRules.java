package fr.riege.ebsl.common.pathfinding.parkour;

public final class ParkourJumpRules {
    private ParkourJumpRules() {
    }

    public static RuleResult evaluate(int dx, int dz, double verticalDelta) {
        if (!ParkourGeometry.isCandidateOffset(dx, dz)) {
            return RuleResult.rejected("unsupported parkour gap");
        }

        int gap = ParkourGeometry.gapBlocks(dx, dz);
        int dyBucket = dyBucket(verticalDelta);
        boolean diagonal = ParkourGeometry.isDiagonal(dx, dz);

        if (dyBucket > 1 || dyBucket < -5) {
            return RuleResult.rejected("parkour vertical delta too high");
        }

        if (diagonal) {
            if (gap > 2) {
                return RuleResult.rejected("diagonal gap above 2 is too hard");
            }
            return RuleResult.accepted(false, gap == 2 && dyBucket < 0);
        }

        if (gap > 3) {
            return RuleResult.rejected("cardinal gap above 3 is too hard");
        }
        return RuleResult.accepted(false, gap == 3 && dyBucket == 0);
    }

    private static int dyBucket(double verticalDelta) {
        if (verticalDelta > 0.50) {
            return 1;
        }
        if (verticalDelta < -0.50) {
            return (int) Math.floor(verticalDelta + 0.50);
        }
        return 0;
    }

    public record RuleResult(boolean accepted, boolean requiresApproach, boolean forceReach, String reason) {
        static RuleResult accepted(boolean requiresApproach) {
            return accepted(requiresApproach, false);
        }

        static RuleResult accepted(boolean requiresApproach, boolean forceReach) {
            return new RuleResult(true, requiresApproach, forceReach, "ok");
        }

        static RuleResult rejected(String reason) {
            return new RuleResult(false, false, false, reason);
        }
    }
}
