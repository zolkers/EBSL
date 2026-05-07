package fr.riege.ebsl.common.platform.layer;

import fr.riege.ebsl.common.domain.entity.EntitySnapshot;

import java.util.List;

public interface IEntityLayer {
    default List<EntitySnapshot> entitiesForRendering() {
        return List.of();
    }
}
