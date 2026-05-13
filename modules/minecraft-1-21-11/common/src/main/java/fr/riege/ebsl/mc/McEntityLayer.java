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

package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.domain.entity.EntitySnapshot;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.world.layer.IEntityLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

final class McEntityLayer implements IEntityLayer {
    private final Minecraft client;

    McEntityLayer(Minecraft client) {
        this.client = client;
    }

    @Override
    public List<EntitySnapshot> entitiesForRendering() {
        if (client.level == null) return List.of();
        List<EntitySnapshot> out = new ArrayList<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            out.add(snapshot(entity));
        }
        return out;
    }

    private static EntitySnapshot snapshot(Entity entity) {
        Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        AABB box = entity.getBoundingBox();
        boolean living = entity instanceof LivingEntity;
        float health = living ? ((LivingEntity) entity).getHealth() : 0.0f;
        var eye = entity.getEyePosition();
        Component displayName = entity.getDisplayName();
        return new EntitySnapshot(
            entity.getId(),
            typeId.toString(),
            displayName == null ? "" : displayName.getString(),
            entity.getName().getString(),
            new Vec3d(entity.getX(), entity.getY(), entity.getZ()),
            new Vec3d(eye.x, eye.y, eye.z),
            box.minX,
            box.minY,
            box.minZ,
            box.maxX,
            box.maxY,
            box.maxZ,
            living,
            entity instanceof Mob,
            entity.isAlive(),
            entity.isRemoved(),
            health);
    }
}
