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

package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.feature.scripting.conditions.EbslConditionOperatorRegistry;
import fr.riege.ebsl.common.feature.scripting.parser.EbslSyntax;
import fr.riege.ebsl.common.feature.scripting.registry.EbslSensorRegistry;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.common.pathfinding.goal.GoalNear;
import fr.riege.ebsl.common.pathfinding.goal.GoalXZ;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.security.SecureRandom;

public final class EbslScriptRuntime {
    private final EbslPlatform platform;
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, List<Object>> lists = new HashMap<>();
    private final SecureRandom random = new SecureRandom();
    private boolean stopped;

    EbslScriptRuntime(EbslPlatform platform) {
        this.platform = platform;
    }

    public EbslPlatform platform() {
        return platform;
    }

    public NavigationService navigation() {
        return EbslServices.navigation();
    }

    public boolean stopped() {
        return stopped;
    }

    public void stop() {
        stopped = true;
        navigation().stop(false);
        platform.input().releaseGameplayKeys();
    }

    public Object variable(String name) {
        return variables.getOrDefault(name, 0.0);
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public List<Object> list(String name) {
        return lists.computeIfAbsent(name, ignored -> new ArrayList<>());
    }

    public boolean truthy(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return Math.abs(n.doubleValue()) > 1.0E-9;
        if (value instanceof String s) return !s.isBlank() && !"false".equalsIgnoreCase(s);
        return value != null;
    }

    public double number(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof Boolean b) return Boolean.TRUE.equals(b) ? 1.0 : 0.0;
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public Object value(String token) {
        if (token == null) return "";
        if (token.startsWith(EbslSyntax.VARIABLE_PREFIX)) return variable(token.substring(EbslSyntax.VARIABLE_PREFIX.length()));
        if ("true".equalsIgnoreCase(token)) return true;
        if ("false".equalsIgnoreCase(token)) return false;
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException ignored) {
            return token;
        }
    }

    public boolean condition(List<String> tokens) {
        if (tokens.isEmpty()) return false;
        String op = tokens.get(0).toLowerCase(Locale.ROOT);
        if (op.startsWith("sensor_")) {
            return sensor(op, tokens.subList(1, tokens.size()));
        }
        if (tokens.size() == 1) {
            return truthy(value(tokens.get(0)));
        }
        return tokens.size() >= 3
            && EbslConditionOperatorRegistry.evaluate(tokens.get(1), this, value(tokens.get(0)), value(tokens.get(2)));
    }

    public boolean sensor(String id, List<String> args) {
        return EbslSensorRegistry.evaluate(id, this, args);
    }

    public void startNavigation(List<String> args, boolean precise) {
        if (args.size() < 2) {
            return;
        }
        if (args.size() == 2) {
            int x = (int) Math.floor(number(value(args.get(0))));
            int z = (int) Math.floor(number(value(args.get(1))));
            navigation().startNavigation(NavigationRequest.builder(new GoalXZ(x, z))
                .allowReplan(true)
                .preciseGoalTolerance(precise ? 0.15 : 0.5)
                .build());
            return;
        }
        int x = (int) Math.floor(number(value(args.get(0))));
        int y = (int) Math.floor(number(value(args.get(1))));
        int z = (int) Math.floor(number(value(args.get(2))));
        navigation().startNavigation(NavigationRequest.builder(precise ? new GoalNear(x, y, z, 0.15) : new GoalBlock(x, y, z))
            .allowReplan(true)
            .preciseGoalTolerance(precise ? 0.15 : 0.5)
            .build());
    }

    public void come() {
        Vec3d pos = platform.player().position();
        navigation().startNavigation(NavigationRequest.builder(new GoalBlock(
                (int) Math.floor(pos.x()),
                (int) Math.floor(pos.y()),
                (int) Math.floor(pos.z())))
            .mode(NavigationModeType.WALK)
            .allowReplan(true)
            .build());
    }

    public double random(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    public double argNumber(List<String> args, int index, double fallback) {
        return index < args.size() ? number(value(args.get(index))) : fallback;
    }

}
