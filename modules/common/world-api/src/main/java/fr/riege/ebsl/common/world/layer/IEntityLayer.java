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

package fr.riege.ebsl.common.world.layer;

import fr.riege.ebsl.common.domain.entity.EntitySnapshot;

import java.util.List;

/**
 * Provides entity snapshots to targeting and rendering systems.
 *
 * <p>The default filters derive targetable, living, and hostile-like views from the platform-provided entity list.</p>
 */
public interface IEntityLayer {
    /**
     * Returns entity snapshots that may be displayed by rendering overlays.
 *
     * @return the requested values
     */
    default List<EntitySnapshot> entitiesForRendering() {
        return List.of();
    }

    /**
     * Returns entity snapshots that may be considered by targeting logic.
 *
     * @return the requested values
     */
    default List<EntitySnapshot> entitiesForTargeting() {
        return entitiesForRendering().stream()
            .filter(EntitySnapshot::alive)
            .filter(entity -> !entity.removed())
            .toList();
    }

    /**
     * Returns targetable entity snapshots that represent living entities.
 *
     * @return the requested values
     */
    default List<EntitySnapshot> livingEntitiesForTargeting() {
        return entitiesForTargeting().stream()
            .filter(EntitySnapshot::living)
            .filter(entity -> entity.health() > 0.0f)
            .toList();
    }

    /**
     * Returns targetable entity snapshots that represent hostile or mob-like entities.
 *
     * @return the requested values
     */
    default List<EntitySnapshot> mobsForTargeting() {
        return livingEntitiesForTargeting().stream()
            .filter(EntitySnapshot::mob)
            .toList();
    }
}
