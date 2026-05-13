package fr.riege.ebsl.common.world.layer;

import fr.riege.ebsl.common.domain.entity.EntitySnapshot;

import java.util.List;

/**
 * Defines the contract for {@code IEntityLayer} implementations.
 */
public interface IEntityLayer {
    default List<EntitySnapshot> entitiesForRendering() {
        return List.of();
    }

    default List<EntitySnapshot> entitiesForTargeting() {
        return entitiesForRendering().stream()
            .filter(EntitySnapshot::alive)
            .filter(entity -> !entity.removed())
            .toList();
    }

    default List<EntitySnapshot> livingEntitiesForTargeting() {
        return entitiesForTargeting().stream()
            .filter(EntitySnapshot::living)
            .filter(entity -> entity.health() > 0.0f)
            .toList();
    }

    default List<EntitySnapshot> mobsForTargeting() {
        return livingEntitiesForTargeting().stream()
            .filter(EntitySnapshot::mob)
            .toList();
    }
}
