package fr.riege.ebsl.common.navigation.runtime.headless;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadlessNavigationApiTest {
    @Test
    void plannerBuildsUsablePathFromHeadlessWorld() {
        HeadlessWorldLayer world = HeadlessWorldLayer.flat(63);
        PathPlanningService planner = new PathPlanningService(world);

        var plan = planner.plan(
            planner.positionFromEntity(0.5, 64.0, 0.5),
            planner.resolveTarget(new fr.riege.ebsl.common.pathfinding.wrapper.PathPosition(4, 64, 0)),
            PathPlannerOptions.defaults().toBuilder().async(false).build()
        ).toCompletableFuture().join();

        assertTrue(plan.usable(), "headless planner should expose a usable path");
        assertFalse(plan.navigationNodes().isEmpty(), "processed navigation nodes should be populated");
    }

    @Test
    void entityNavigationServiceEmitsMovementIntentForServerAdapters() {
        HeadlessWorldLayer world = HeadlessWorldLayer.flat(63);
        HeadlessActor actor = new HeadlessActor(new Vec3d(0.5, 64.0, 0.5));
        HeadlessMotor motor = new HeadlessMotor(actor);
        EntityNavigationService service = new EntityNavigationService(
            new PathPlanningService(world),
            actor,
            motor);
        service.setPlannerOptions(PathPlannerOptions.defaults().toBuilder().async(false).build());

        service.startBlockGoal(4, 64, 0);
        service.tick();

        assertTrue(service.isNavigating(), "service should be executing after a usable path is found");
        assertTrue(motor.lastIntent().velocity().x() > 0.0, "server motor should receive a positive X velocity");
    }
}
