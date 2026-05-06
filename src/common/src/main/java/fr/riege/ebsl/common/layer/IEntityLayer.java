package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.entity.EntitySnapshot;

import java.util.List;

public interface IEntityLayer {
    default List<EntitySnapshot> entitiesForRendering() {
        return List.of();
    }
}
