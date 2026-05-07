package fr.riege.ebsl.common.api.navigation.runtime;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.platform.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationActor;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationMotor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessActor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessMotor;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessNavigationService;
import fr.riege.ebsl.common.navigation.runtime.headless.HeadlessWorldLayer;

@EbslApiSurface(EbslApiSurface.Domain.RUNTIME)
public final class RuntimeApi {
    public RuntimeApi() {
    }

    @EbslApiOperation("Create a mutable headless world.")
    public HeadlessWorldLayer headlessWorld() {
        return new HeadlessWorldLayer();
    }

    @EbslApiOperation("Create a flat headless world with a solid floor.")
    public HeadlessWorldLayer flatHeadlessWorld(int floorY) {
        return HeadlessWorldLayer.flat(floorY);
    }

    @EbslApiOperation("Create a headless navigation actor.")
    public HeadlessActor headlessActor(Vec3d position) {
        return new HeadlessActor(position);
    }

    @EbslApiOperation("Create a headless motor for an actor.")
    public HeadlessMotor headlessMotor(HeadlessActor actor) {
        return new HeadlessMotor(actor);
    }

    @EbslApiOperation("Create a complete headless navigation service.")
    public HeadlessNavigationService headlessNavigation(HeadlessWorldLayer world, HeadlessActor actor) {
        return new HeadlessNavigationService(world, actor);
    }

    @EbslApiOperation("Create a server/entity navigation service over custom adapters.")
    public EntityNavigationService entityNavigation(IWorldLayer world, NavigationActor actor, NavigationMotor motor) {
        return new EntityNavigationService(new PathPlanningService(world), actor, motor);
    }

    @EbslApiOperation("Create a server/entity navigation service with follower options.")
    public EntityNavigationService entityNavigation(IWorldLayer world,
                                                    NavigationActor actor,
                                                    NavigationMotor motor,
                                                    EntityFollowerOptions options) {
        return new EntityNavigationService(new PathPlanningService(world), actor, motor, options, Runnable::run);
    }
}
