package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.core.registry.EnumRegistry;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.LayerPathProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class NodeProcessorRegistry {
    private static final EnumRegistry<NodeProcessorType, Supplier<NodeProcessor>> PROCESSORS =
        new EnumRegistry<>(NodeProcessorType.class, null);

    static {
        register(NodeProcessorType.LAYER, LayerPathProcessor::new);
    }

    private NodeProcessorRegistry() {
    }

    public static void register(NodeProcessorType type, Supplier<NodeProcessor> factory) {
        PROCESSORS.register(type, factory);
    }

    public static NodeProcessor create(NodeProcessorType type) {
        Supplier<NodeProcessor> factory = PROCESSORS.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown node processor: " + type);
        }
        return factory.get();
    }

    public static List<NodeProcessor> createAll(NodeProcessorType... types) {
        return Arrays.stream(types)
            .map(NodeProcessorRegistry::create)
            .toList();
    }

    public static List<NodeProcessor> createStandardProcessors() {
        return createAll(NodeProcessorType.LAYER);
    }
}
