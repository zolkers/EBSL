/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.aim.AimProcessor;
import fr.riege.ebsl.common.feature.aim.BlockAimTarget;
import fr.riege.ebsl.common.feature.aim.BlockAimTargeting;
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

@EbslNodeDefinition(value = EbslNodeType.AIM_AT_BLOCK, aliases = {"aim_block", "aim_nearest_block"})
public final class AimAtBlockNode extends AbstractEbslNode {
    private final AimProcessor aimProcessor = new AimProcessor();
    private BlockAimTarget target;

    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("block_id", "Block", "minecraft:oak_leaves"));
        registerSetting(new IntSetting("search_radius", "Search Radius", 32, 1, 128));
        registerSetting(new IntSetting("ticks", "Ticks", 10, 1, 200));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        aimProcessor.reset();
        target = findTarget(invocation);
        tick(invocation);
        return target == null ? 0 : duration(invocation);
    }

    @Override
    public void tick(EbslNodeInvocation invocation) {
        if (target != null) {
            aimProcessor.aimAt(invocation.runtime().platform(), target.aimPoint());
        }
    }

    @Override
    public void finish(EbslNodeInvocation invocation) {
        target = null;
        aimProcessor.reset();
    }

    private BlockAimTarget findTarget(EbslNodeInvocation invocation) {
        if (invocation.args().isEmpty()) {
            return null;
        }
        String blockId = invocation.runtime().value(invocation.arg(0)).toString();
        int radius = (int) invocation.runtime().argNumber(invocation.args(), 1, 32.0);
        return BlockAimTargeting.nearest(invocation.runtime().platform(), blockId, radius, true);
    }

    private int duration(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 3) {
            return EbslDuration.ticks(invocation.arg(2));
        }
        return 10;
    }
}
