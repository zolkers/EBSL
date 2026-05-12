package fr.riege.ebsl.common.pathfinding.quality;

public enum PathQualityPlanningMode {
    OFF(false, false),
    BALANCED(true, true),
    CAUTIOUS(true, true);

    private final boolean costAware;
    private final boolean retryPoorPlans;

    PathQualityPlanningMode(boolean costAware, boolean retryPoorPlans) {
        this.costAware = costAware;
        this.retryPoorPlans = retryPoorPlans;
    }

    public boolean costAware() {
        return costAware;
    }

    public boolean retryPoorPlans() {
        return retryPoorPlans;
    }
}
