package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

final class MovementRiskQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "movement_risk";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        List<Node> nodes = context.rawNodes().isEmpty() ? context.navigationNodes() : context.rawNodes();
        if (nodes.size() <= 1) {
            boolean hasPositionPath = context.positions().size() > 1;
            return new PathQualityContribution(id(), hasPositionPath || !nodes.isEmpty() ? 1.0 : 0.0, 1.4, "no typed moves");
        }
        double risk = 0.0;
        for (int i = 1; i < nodes.size(); i++) {
            risk += MovementRiskScorer.risk(nodes.get(i).moveType);
        }
        double averageRisk = risk / (nodes.size() - 1);
        double score = Math.clamp(1.0 - averageRisk, 0.0, 1.0);
        return new PathQualityContribution(id(), score, 1.4, String.format("risk %.2f", averageRisk));
    }
}
