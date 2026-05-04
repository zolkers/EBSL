package fr.riege.ebsl.general.task;

import fr.riege.ebsl.general.task.processor.EntityAimProcessor;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.DoubleSetting;
import fr.riege.ebsl.settings.EnumSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Settingable;
import fr.riege.ebsl.settings.StringSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.StreamSupport;

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

    private Entity explicitTarget;
    private Entity currentTarget;
    private BlockPos lastGoal;
    private int replanCooldown;
    private int stableDistanceTicks;

    private SpaceMobTask() {
    }

    @Override public String id() { return "space_mob"; }
    @Override public String displayName() { return "Space Mob"; }
    @Override public String description() { return "Keeps a configured distance from a mob while aiming at it."; }
    @Override public boolean isEnabled() { return enabledSetting.value(); }
    @Override public void setEnabled(boolean enabled) { enabledSetting.setValue(enabled); }

    public void setExplicitTarget(Entity entity) {
        explicitTarget = entity;
        currentTarget = entity;
    }

    @Override
    public void tick(Minecraft mc) {
        if (!isEnabled() || mc.player == null || mc.level == null) {
            return;
        }
        if (replanCooldown > 0) {
            replanCooldown--;
        }

        Entity target = selectTarget(mc);
        if (!isUsableTarget(mc, target)) {
            currentTarget = null;
            aimProcessor.reset();
            return;
        }

        if (currentTarget != target) {
            lastGoal = null;
            stableDistanceTicks = 0;
        }
        currentTarget = target;
        maintainDistance(mc, target);
    }

    @Override
    public void render(Minecraft mc) {
        if (!isEnabled() || mc.player == null || mc.level == null) {
            return;
        }
        if (!isUsableTarget(mc, currentTarget)) {
            aimProcessor.reset();
            return;
        }
        aimProcessor.aimAt(mc, currentTarget);
    }

    @Override
    public void onDisable() {
        currentTarget = null;
        lastGoal = null;
        replanCooldown = 0;
        stableDistanceTicks = 0;
        aimProcessor.reset();
        PathfindingManager.stop(false);
    }

    private Entity selectTarget(Minecraft mc) {
        if (trackUntilDeath.value() && isUsableTarget(mc, currentTarget)) {
            return currentTarget;
        }
        if (isUsableTarget(mc, explicitTarget)) {
            return explicitTarget;
        }
        if (targetMode.value() == MobTargetMode.ENTITY_NAME && !targetName.value().isBlank()) {
            return findByName(mc);
        }
        return findClosestMob(mc);
    }

    private Entity findClosestMob(Minecraft mc) {
        double maxDistanceSq = searchRadius.value() * searchRadius.value();
        return StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
            .filter(entity -> entity instanceof Mob)
            .filter(entity -> isUsableTarget(mc, entity))
            .filter(entity -> entity.distanceToSqr(mc.player) <= maxDistanceSq)
            .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(mc.player)))
            .orElse(null);
    }

    private Entity findByName(Minecraft mc) {
        String query = normalize(targetName.value());
        if (query.isEmpty()) {
            return null;
        }
        double maxDistanceSq = searchRadius.value() * searchRadius.value();
        return StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
            .filter(entity -> entity instanceof LivingEntity)
            .filter(entity -> isUsableTarget(mc, entity))
            .filter(entity -> entity.distanceToSqr(mc.player) <= maxDistanceSq)
            .filter(entity -> matchesName(entity, query))
            .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(mc.player)))
            .orElse(null);
    }

    private boolean matchesName(Entity entity, String query) {
        String display = normalize(entity.getDisplayName().getString());
        String name = normalize(entity.getName().getString());
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String typeId = id != null ? normalize(id.toString()) : "";
        String path = id != null ? normalize(id.getPath()) : "";
        return display.contains(query) || name.contains(query) || typeId.equals(query) || path.equals(query);
    }

    private boolean isUsableTarget(Minecraft mc, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        return entity != mc.player && entity.isAlive() && !entity.isRemoved() && living.getHealth() > 0.0f;
    }

    private void maintainDistance(Minecraft mc, Entity target) {
        Vec3 playerPos = mc.player.position();
        Vec3 targetPos = target.position();
        double dx = playerPos.x - targetPos.x;
        double dz = playerPos.z - targetPos.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double wanted = distance.value();
        double distanceError = Math.abs(horizontalDistance - wanted);

        if (distanceError <= tolerance.value()) {
            stableDistanceTicks++;
            if (PathfindingManager.isNavigating() && stableDistanceTicks >= STABLE_DISTANCE_TICKS_BEFORE_STOP) {
                PathfindingManager.stop(false);
                lastGoal = null;
            }
            return;
        }
        stableDistanceTicks = 0;
        if (replanCooldown > 0) {
            return;
        }

        Vec3 away = horizontalDistance > 0.001
            ? new Vec3(dx / horizontalDistance, 0.0, dz / horizontalDistance)
            : Vec3.directionFromRotation(0.0f, mc.player.getYRot()).multiply(-1.0, 0.0, -1.0);
        Vec3 desired = targetPos.add(away.x * wanted, 0.0, away.z * wanted);
        BlockPos goal = BlockPos.containing(desired.x, mc.player.getY(), desired.z);
        if (!shouldReplan(goal, distanceError, tolerance.value())) {
            return;
        }

        lastGoal = goal;
        replanCooldown = MIN_REPLAN_TICKS;
        PathfindingManager.startGoal(mc, NavigationRequest.builder(new GoalBlock(goal.getX(), goal.getY(), goal.getZ()))
            .allowParkour(false).allowRotation(false).allowSneak(false).allowReplan(true).allowFall(false)
            .build());
    }

    private boolean shouldReplan(BlockPos goal, double distanceError, double tolerance) {
        if (!PathfindingManager.isNavigating()) {
            return true;
        }
        if (lastGoal == null) {
            return true;
        }
        double movedSq = horizontalDistanceSq(lastGoal, goal);
        if (distanceError >= tolerance * HARD_ERROR_MULTIPLIER) {
            return movedSq >= 1.0;
        }
        return movedSq >= GOAL_REPLAN_DISTANCE_SQ;
    }

    private static double horizontalDistanceSq(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
