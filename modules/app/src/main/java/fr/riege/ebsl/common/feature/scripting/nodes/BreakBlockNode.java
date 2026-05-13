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

package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.domain.world.TargetedBlock;
import fr.riege.ebsl.common.feature.aim.BlockAimTargeting;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

import java.util.List;
import java.util.Locale;

@EbslNodeDefinition(EbslNodeType.BREAK_BLOCK)
public final class BreakBlockNode extends AbstractEbslNode {
    private static final String DEFAULT_MAX_DURATION = "30s";

    private TargetedBlock target;
    private BlockId fallbackTarget;

    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("block", "Block", ""));
        registerSetting(new StringSetting("max_duration", "Max Duration", DEFAULT_MAX_DURATION));
    }

    @Override
    public boolean releasesGameplayKeys() {
        return true;
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        target = invocation.runtime().platform().player().targetedBlockHit();
        fallbackTarget = target == null ? invocation.runtime().platform().player().targetedBlock() : target.block();
        TimedInputNode.press(invocation.runtime(), EbslInputKey.ATTACK);
        invocation.runtime().platform().input().attackTargetedBlock();
        return EbslDuration.ticks(maxDuration(invocation.args()));
    }

    @Override
    public void tick(EbslNodeInvocation invocation) {
        TimedInputNode.press(invocation.runtime(), EbslInputKey.ATTACK);
        invocation.runtime().platform().input().attackTargetedBlock();
    }

    @Override
    public boolean isComplete(EbslNodeInvocation invocation) {
        String expected = expectedBlock(invocation);
        if (target != null) {
            if (invocation.runtime().platform().world().isAir(target.x(), target.y(), target.z())) {
                return true;
            }
            BlockId current = invocation.runtime().platform().world().getBlock(target.x(), target.y(), target.z());
            return !matchesExpected(current, expected, target.block());
        }

        BlockId currentTarget = invocation.runtime().platform().player().targetedBlock();
        if (currentTarget == null) {
            return true;
        }
        return !matchesExpected(currentTarget, expected, fallbackTarget);
    }

    @Override
    public void finish(EbslNodeInvocation invocation) {
        target = null;
        fallbackTarget = null;
    }

    private static boolean matchesExpected(BlockId current, String expected, BlockId captured) {
        if (current == null) {
            return false;
        }
        if (!expected.isBlank()) {
            return BlockAimTargeting.matches(current, expected);
        }
        return captured != null && current.toString().equalsIgnoreCase(captured.toString());
    }

    private static String expectedBlock(EbslNodeInvocation invocation) {
        List<String> args = invocation.args();
        if (args.isEmpty() || looksLikeDuration(args.getFirst())) {
            return "";
        }
        return invocation.runtime().value(args.getFirst()).toString().trim();
    }

    private static String maxDuration(List<String> args) {
        if (args.isEmpty()) {
            return DEFAULT_MAX_DURATION;
        }
        if (looksLikeDuration(args.getFirst())) {
            return args.getFirst();
        }
        return args.size() > 1 ? args.get(1) : DEFAULT_MAX_DURATION;
    }

    private static boolean looksLikeDuration(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.endsWith("t") || normalized.endsWith("s");
    }
}
