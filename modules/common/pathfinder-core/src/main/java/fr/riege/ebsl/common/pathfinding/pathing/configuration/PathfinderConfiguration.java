package fr.riege.ebsl.common.pathfinding.pathing.configuration;

import fr.riege.ebsl.common.pathfinding.movement.DefaultMovementTypeClassifier;
import fr.riege.ebsl.common.pathfinding.movement.MovementTypeClassifier;
import fr.riege.ebsl.common.pathfinding.pathing.INeighborStrategy;
import fr.riege.ebsl.common.pathfinding.pathing.NeighborStrategies;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicWeights;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.LinearHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.quality.DefaultMovementCostModel;
import fr.riege.ebsl.common.pathfinding.quality.MovementCostModel;

import java.util.Collections;
import java.util.List;

public final class PathfinderConfiguration {

    public final int                  maxIterations;
    public final int                  maxLength;
    public final boolean              async;
    public final boolean              fallback;
    public final boolean              profiling;
    public final boolean              earlyFallback;
    public final int                  earlyFallbackIterations;
    public final int                  earlyFallbackMinPathNodes;
    public final double               earlyFallbackMinProgressRatio;
    public final long                 maxCalculationTimeMs;
    public final double               qualityRiskCostWeight;
    public final double               qualityTerrainCostWeight;
    public final NavigationPointProvider provider;
    public final HeuristicWeights     heuristicWeights;
    public final List<NodeProcessor>  processors;
    public final INeighborStrategy    neighborStrategy;
    public final IHeuristicStrategy   heuristicStrategy;
    public final MovementTypeClassifier movementClassifier;
    public final MovementCostModel    movementCostModel;

    private PathfinderConfiguration(Builder b) {
        this.maxIterations    = b.maxIterations;
        this.maxLength        = b.maxLength;
        this.async            = b.async;
        this.fallback         = b.fallback;
        this.profiling        = b.profiling;
        this.earlyFallback    = b.earlyFallback;
        this.earlyFallbackIterations = b.earlyFallbackIterations;
        this.earlyFallbackMinPathNodes = b.earlyFallbackMinPathNodes;
        this.earlyFallbackMinProgressRatio = b.earlyFallbackMinProgressRatio;
        this.maxCalculationTimeMs = b.maxCalculationTimeMs;
        this.qualityRiskCostWeight = b.qualityRiskCostWeight;
        this.qualityTerrainCostWeight = b.qualityTerrainCostWeight;
        this.provider         = b.provider;
        this.heuristicWeights = b.heuristicWeights;
        this.processors       = Collections.unmodifiableList(b.processors);
        this.neighborStrategy = b.neighborStrategy;
        this.heuristicStrategy= b.heuristicStrategy;
        this.movementClassifier = b.movementClassifier;
        this.movementCostModel = b.movementCostModel;
    }

    public static final PathfinderConfiguration DEFAULT = new Builder().build();

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int                  maxIterations    = 50000;
        private int                  maxLength        = 5000;
        private boolean              async            = false;
        private boolean              fallback         = true;
        private boolean              profiling        = false;
        private boolean              earlyFallback    = false;
        private int                  earlyFallbackIterations = 0;
        private int                  earlyFallbackMinPathNodes = 8;
        private double               earlyFallbackMinProgressRatio = 0.08;
        private long                 maxCalculationTimeMs = 0L;
        private double               qualityRiskCostWeight = 0.0;
        private double               qualityTerrainCostWeight = 0.0;
        private NavigationPointProvider provider      = DefaultNavigationPointProvider.INSTANCE;
        private HeuristicWeights     heuristicWeights = HeuristicWeights.DEFAULT_WEIGHTS;
        private List<NodeProcessor>  processors       = List.of();
        private INeighborStrategy    neighborStrategy = NeighborStrategies.VERTICAL_AND_HORIZONTAL;
        private IHeuristicStrategy   heuristicStrategy= new LinearHeuristicStrategy();
        private MovementTypeClassifier movementClassifier = DefaultMovementTypeClassifier.INSTANCE;
        private MovementCostModel    movementCostModel = DefaultMovementCostModel.INSTANCE;

        public Builder maxIterations(int v)            { this.maxIterations    = v; return this; }
        public Builder maxLength(int v)                { this.maxLength        = v; return this; }
        public Builder async(boolean v)                { this.async            = v; return this; }
        public Builder fallback(boolean v)             { this.fallback         = v; return this; }
        public Builder profiling(boolean v)            { this.profiling        = v; return this; }
        public Builder earlyFallback(boolean v)        { this.earlyFallback    = v; return this; }
        public Builder earlyFallbackIterations(int v)  { this.earlyFallbackIterations = v; return this; }
        public Builder earlyFallbackMinPathNodes(int v){ this.earlyFallbackMinPathNodes = v; return this; }
        public Builder earlyFallbackMinProgressRatio(double v) {
            this.earlyFallbackMinProgressRatio = v;
            return this;
        }
        public Builder maxCalculationTimeMs(long v)    { this.maxCalculationTimeMs = v; return this; }
        public Builder qualityRiskCostWeight(double v) { this.qualityRiskCostWeight = Math.max(0.0, v); return this; }
        public Builder qualityTerrainCostWeight(double v) { this.qualityTerrainCostWeight = Math.max(0.0, v); return this; }
        public Builder provider(NavigationPointProvider v) { this.provider     = v; return this; }
        public Builder heuristicWeights(HeuristicWeights v){ this.heuristicWeights = v; return this; }
        public Builder processors(List<NodeProcessor> v)   { this.processors   = v; return this; }
        public Builder neighborStrategy(INeighborStrategy v){ this.neighborStrategy = v; return this; }
        public Builder heuristicStrategy(IHeuristicStrategy v){ this.heuristicStrategy = v; return this; }
        public Builder movementClassifier(MovementTypeClassifier v) {
            this.movementClassifier = v == null ? DefaultMovementTypeClassifier.INSTANCE : v;
            return this;
        }
        public Builder movementCostModel(MovementCostModel v) {
            this.movementCostModel = v == null ? DefaultMovementCostModel.INSTANCE : v;
            return this;
        }

        public PathfinderConfiguration build() { return new PathfinderConfiguration(this); }
    }
}
