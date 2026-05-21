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

package fr.riege.ebsl.common.automation.aim;

import fr.riege.ebsl.common.domain.entity.EntitySnapshot;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.platform.EbslPlatform;

public final class EntityAimProcessor {
    private final AimProcessor aimProcessor = new AimProcessor();

    public boolean aimAt(EbslPlatform platform, EntitySnapshot target) {
        if (target == null || !platform.player().isAlive()) {
            return false;
        }
        Vec3d aimPoint = visibleAimPoint(platform, target);
        if (aimPoint == null) {
            aimProcessor.reset();
            return false;
        }
        aimProcessor.aimAt(platform, aimPoint);
        return true;
    }

    private static Vec3d visibleAimPoint(EbslPlatform platform, EntitySnapshot target) {
        Vec3d eye = target.eyePosition();
        if (platform.world().hasLineOfSight(platform.player().eyePosition(), eye)) {
            return eye;
        }

        double x = (target.minX() + target.maxX()) * 0.5;
        double z = (target.minZ() + target.maxZ()) * 0.5;
        Vec3d upperBody = new Vec3d(x, target.minY() + target.bbHeight() * 0.75, z);
        if (platform.world().hasLineOfSight(platform.player().eyePosition(), upperBody)) {
            return upperBody;
        }

        Vec3d centerMass = new Vec3d(x, target.minY() + target.bbHeight() * 0.5, z);
        if (platform.world().hasLineOfSight(platform.player().eyePosition(), centerMass)) {
            return centerMass;
        }

        Vec3d lowerBody = new Vec3d(x, target.minY() + target.bbHeight() * 0.35, z);
        return platform.world().hasLineOfSight(platform.player().eyePosition(), lowerBody) ? lowerBody : null;
    }

    public void reset() {
        aimProcessor.reset();
    }
}
