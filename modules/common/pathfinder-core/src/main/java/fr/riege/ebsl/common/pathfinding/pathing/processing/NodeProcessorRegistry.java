/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.core.registry.EnumRegistry;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.LayerPathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.QualityAwarePathProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class NodeProcessorRegistry {
    private static final EnumRegistry<NodeProcessorType, Supplier<NodeProcessor>> PROCESSORS =
        new EnumRegistry<>(NodeProcessorType.class, null);

    static {
        register(NodeProcessorType.LAYER, LayerPathProcessor::new);
        register(NodeProcessorType.QUALITY_AWARE, QualityAwarePathProcessor::new);
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
        return createAll(NodeProcessorType.LAYER, NodeProcessorType.QUALITY_AWARE);
    }
}
