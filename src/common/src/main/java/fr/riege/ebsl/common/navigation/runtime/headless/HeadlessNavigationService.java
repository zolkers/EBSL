package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;

public final class HeadlessNavigationService extends EntityNavigationService {
    public HeadlessNavigationService(HeadlessWorldLayer world, HeadlessActor actor) {
        this(world, actor, new HeadlessMotor(actor));
    }

    public HeadlessNavigationService(HeadlessWorldLayer world, HeadlessActor actor, HeadlessMotor motor) {
        super(new PathPlanningService(world), actor, motor, EntityFollowerOptions.defaults(), Runnable::run);
    }
}
