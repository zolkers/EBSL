package fr.riege.ebsl.common.feature.task;

import fr.riege.ebsl.common.domain.entity.EntitySnapshot;
import fr.riege.ebsl.common.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.core.settings.*;
import fr.riege.ebsl.common.feature.aim.EntityAimProcessor;

import java.util.Comparator;
import java.util.Locale;

public final class SpaceMobTask extends Settingable implements BotTask {
    public static final SpaceMobTask INSTANCE = new SpaceMobTask();

    private static final int MIN_REPLAN_TICKS = 12;
    private static final int STABLE_DISTANCE_TICKS_BEFORE_STOP = 6;
    private static final double GOAL_REPLAN_DISTANCE_SQ = 4.0;
    private static final double HARD_ERROR_MULTIPLIER = 3.0;

    private final EntityAimProcessor aimProcessor = new EntityAimProcessor();
    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final EnumSetting<MobTargetMode> targetMode = registerSetting(
        new EnumSetting<>("target_mode", "Target mode", MobTargetMode.CLOSEST_MOB, MobTargetMode.class));
    private final StringSetting targetName = registerSetting(new StringSetting("target_name", "Entity name", ""));
    private final DoubleSetting distance = registerSetting(new DoubleSetting("distance", "Distance", 3.0, 1.0, 24.0));
    private final DoubleSetting tolerance = registerSetting(new DoubleSetting("tolerance", "Tolerance", 0.35, 0.1, 4.0));
    private final IntSetting searchRadius = registerSetting(new IntSetting("search_radius", "Search radius", 32, 4, 128));
    private final BooleanSetting trackUntilDeath = registerSetting(new BooleanSetting("track_until_death", "Track until death", false));

    private Integer currentTargetId;
    private GoalKey lastGoal;
    private int replanCooldown;
    private int stableDistanceTicks;

    private SpaceMobTask() {
    }

    @Override public String id() { return "space_mob"; }
    @Override public String displayName() { return "Space Mob"; }
    @Override public String description() { return "Keeps a configured distance from a mob while aiming at it."; }
    @Override public boolean isEnabled() { return enabledSetting.value(); }
    @Override public void setEnabled(boolean enabled) { enabledSetting.setValue(enabled); }

    public void configure(MobTargetMode mode, String name, double wantedDistance, double wantedTolerance,
                          int wantedSearchRadius, boolean shouldTrackUntilDeath) {
        targetMode.setValue(mode != null ? mode : MobTargetMode.CLOSEST_MOB);
        targetName.setValue(name != null ? name : "");
        distance.setValue(Math.clamp(wantedDistance, distance.min(), distance.max()));
        tolerance.setValue(Math.clamp(wantedTolerance, tolerance.min(), tolerance.max()));
        searchRadius.setValue(Math.clamp(wantedSearchRadius, searchRadius.min(), searchRadius.max()));
        trackUntilDeath.setValue(shouldTrackUntilDeath);
    }

    @Override
    public void tick(EbslPlatform platform) {
        if (!isEnabled() || !platform.player().isAlive()) {
            return;
        }
        if (replanCooldown > 0) {
            replanCooldown--;
        }

        EntitySnapshot target = selectTarget(platform);
        if (!isUsableTarget(platform, target)) {
            currentTargetId = null;
            aimProcessor.reset();
            return;
        }

        if (!Integer.valueOf(target.id()).equals(currentTargetId)) {
            lastGoal = null;
            stableDistanceTicks = 0;
        }
        currentTargetId = target.id();
        maintainDistance(platform, target);
    }

    @Override
    public void render(EbslPlatform platform) {
        if (!isEnabled() || !platform.player().isAlive()) {
            return;
        }
        EntitySnapshot target = findById(platform, currentTargetId);
        if (!isUsableTarget(platform, target)) {
            aimProcessor.reset();
            return;
        }
        aimProcessor.aimAt(platform, target);
    }

    @Override
    public void onDisable() {
        currentTargetId = null;
        lastGoal = null;
        replanCooldown = 0;
        stableDistanceTicks = 0;
        aimProcessor.reset();
        EbslServices.navigation().stop(false);
    }

    private EntitySnapshot selectTarget(EbslPlatform platform) {
        EntitySnapshot current = findById(platform, currentTargetId);
        if (trackUntilDeath.value() && isUsableTarget(platform, current)) {
            return current;
        }
        if (targetMode.value() == MobTargetMode.ENTITY_NAME && !targetName.value().isBlank()) {
            return findByName(platform);
        }
        return findClosestMob(platform);
    }

    private EntitySnapshot findClosestMob(EbslPlatform platform) {
        double maxDistanceSq = searchRadius.value() * searchRadius.value();
        return platform.entities().entitiesForRendering().stream()
            .filter(EntitySnapshot::mob)
            .filter(entity -> isUsableTarget(platform, entity))
            .filter(entity -> entity.distanceToSq(platform.player().position()) <= maxDistanceSq)
            .min(Comparator.comparingDouble(entity -> entity.distanceToSq(platform.player().position())))
            .orElse(null);
    }

    private EntitySnapshot findByName(EbslPlatform platform) {
        String query = normalize(targetName.value());
        if (query.isEmpty()) return null;
        double maxDistanceSq = searchRadius.value() * searchRadius.value();
        return platform.entities().entitiesForRendering().stream()
            .filter(EntitySnapshot::living)
            .filter(entity -> isUsableTarget(platform, entity))
            .filter(entity -> entity.distanceToSq(platform.player().position()) <= maxDistanceSq)
            .filter(entity -> matchesName(entity, query))
            .min(Comparator.comparingDouble(entity -> entity.distanceToSq(platform.player().position())))
            .orElse(null);
    }

    private EntitySnapshot findById(EbslPlatform platform, Integer id) {
        if (id == null) return null;
        return platform.entities().entitiesForRendering().stream()
            .filter(entity -> entity.id() == id)
            .findFirst()
            .orElse(null);
    }

    private boolean matchesName(EntitySnapshot entity, String query) {
        String display = normalize(entity.displayName());
        String name = normalize(entity.name());
        String typeId = normalize(entity.typeId());
        String path = typeId.contains(":") ? typeId.substring(typeId.indexOf(':') + 1) : typeId;
        return display.contains(query) || name.contains(query) || typeId.equals(query) || path.equals(query);
    }

    private boolean isUsableTarget(EbslPlatform platform, EntitySnapshot entity) {
        Integer playerId = platform.player().entityId();
        return entity != null
            && entity.living()
            && (playerId == null || entity.id() != (int) playerId)
            && entity.alive()
            && !entity.removed()
            && entity.health() > 0.0f;
    }

    private void maintainDistance(EbslPlatform platform, EntitySnapshot target) {
        var playerPos = platform.player().position();
        var targetPos = target.position();
        double dx = playerPos.x() - targetPos.x();
        double dz = playerPos.z() - targetPos.z();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double wanted = distance.value();
        double distanceError = Math.abs(horizontalDistance - wanted);

        if (distanceError <= tolerance.value()) {
            stableDistanceTicks++;
            if (EbslServices.navigation().isNavigating() && stableDistanceTicks >= STABLE_DISTANCE_TICKS_BEFORE_STOP) {
                EbslServices.navigation().stop(false);
                lastGoal = null;
            }
            return;
        }
        stableDistanceTicks = 0;
        if (replanCooldown > 0) {
            return;
        }

        double awayX;
        double awayZ;
        if (horizontalDistance > 0.001) {
            awayX = dx / horizontalDistance;
            awayZ = dz / horizontalDistance;
        } else {
            double yawRad = Math.toRadians(platform.player().yaw());
            awayX = Math.sin(yawRad);
            awayZ = -Math.cos(yawRad);
        }

        int goalX = (int) Math.floor(targetPos.x() + awayX * wanted);
        int goalY = (int) Math.floor(playerPos.y());
        int goalZ = (int) Math.floor(targetPos.z() + awayZ * wanted);
        GoalKey goal = new GoalKey(goalX, goalY, goalZ);
        if (!shouldReplan(goal, distanceError, tolerance.value())) {
            return;
        }

        lastGoal = goal;
        replanCooldown = MIN_REPLAN_TICKS;
        EbslServices.navigation().startNavigation(NavigationRequest.builder(new GoalBlock(goalX, goalY, goalZ))
            .allowParkour(false)
            .allowRotation(false)
            .allowSneak(false)
            .allowReplan(true)
            .allowFall(false)
            .build());
    }

    private boolean shouldReplan(GoalKey goal, double distanceError, double tolerance) {
        if (!EbslServices.navigation().isNavigating()) return true;
        if (lastGoal == null) return true;
        double movedSq = lastGoal.horizontalDistanceSq(goal);
        if (distanceError >= tolerance * HARD_ERROR_MULTIPLIER) {
            return movedSq >= 1.0;
        }
        return movedSq >= GOAL_REPLAN_DISTANCE_SQ;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record GoalKey(int x, int y, int z) {
        double horizontalDistanceSq(GoalKey other) {
            double dx = x - other.x;
            double dz = z - other.z;
            return dx * dx + dz * dz;
        }
    }
}
