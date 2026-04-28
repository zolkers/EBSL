package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

final class ParkourExecutionTelemetry {
    private static final long ANALYTICS_INTERVAL_MS = 100;
    private static final long CHAT_INTERVAL_MS = 450;

    private static long lastAnalyticsTime;
    private static long lastChatTime;
    private static String lastSignature = "";

    private ParkourExecutionTelemetry() {
    }

    static void record(Minecraft mc, List<Node> path, Vec3 playerPos, Node waypoint,
                       int pursuitSegment, int jumpCooldown, boolean allowJumps,
                       String decision) {
        if (mc.player == null || path == null || waypoint == null || waypoint.moveType != Node.MoveType.PARKOUR) {
            return;
        }
        if (!PathfinderSettings.instance().showDebug.value()) {
            return;
        }

        long now = System.currentTimeMillis();
        Metrics metrics = metrics(path, playerPos, waypoint, pursuitSegment);
        String signature = phase(mc) + "|" + decision + "|" + keyState(mc);
        if (now - lastAnalyticsTime < ANALYTICS_INTERVAL_MS && signature.equals(lastSignature)) {
            return;
        }

        String message = String.format(Locale.ROOT,
            "seg=%d %s gap=%d off=%d h=%.2f prog=%.2f rem=%.2f lat=%.2f v=%.3f cd=%d allow=%s next=%s keys=%s pos=%.2f,%.2f,%.2f",
            pursuitSegment,
            decision,
            metrics.gapBlocks,
            metrics.distanceBlocks,
            metrics.horizontalDistance,
            metrics.progress,
            metrics.remaining,
            metrics.lateral,
            metrics.velocityAlong,
            jumpCooldown,
            allowJumps,
            nextMove(path, pursuitSegment),
            keyState(mc),
            playerPos.x,
            playerPos.y,
            playerPos.z);

        AnalyticsEventLog.record("parkour", message);
        lastAnalyticsTime = now;
        lastSignature = signature;

        if (now - lastChatTime >= CHAT_INTERVAL_MS) {
            ClientUtils.sendDebugMessage(mc, "parkour " + message);
            lastChatTime = now;
        }
    }

    private static Metrics metrics(List<Node> path, Vec3 playerPos, Node waypoint, int pursuitSegment) {
        Node takeoff = path.get(Math.max(0, Math.min(pursuitSegment, path.size() - 1)));
        double startX = takeoff.position.centeredX();
        double startZ = takeoff.position.centeredZ();
        double targetX = waypoint.position.centeredX();
        double targetZ = waypoint.position.centeredZ();
        double dirX = targetX - startX;
        double dirZ = targetZ - startZ;
        double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (len < 1.0e-6) {
            return new Metrics(0, 0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        dirX /= len;
        dirZ /= len;
        double fromStartX = playerPos.x - startX;
        double fromStartZ = playerPos.z - startZ;
        double progress = fromStartX * dirX + fromStartZ * dirZ;
        double lateral = fromStartX * -dirZ + fromStartZ * dirX;
        double dx = waypoint.position.centeredX() - playerPos.x;
        double dz = waypoint.position.centeredZ() - playerPos.z;
        int offsetX = waypoint.position.flooredX() - takeoff.position.flooredX();
        int offsetZ = waypoint.position.flooredZ() - takeoff.position.flooredZ();
        int distanceBlocks = ParkourGeometry.distanceBlocks(offsetX, offsetZ);
        int gapBlocks = ParkourGeometry.isDiagonal(offsetX, offsetZ)
            ? ParkourGeometry.diagonalGapBlocks(offsetX, offsetZ)
            : ParkourGeometry.cardinalGapBlocks(offsetX, offsetZ);
        Minecraft mc = Minecraft.getInstance();
        double velocityAlong = mc.player == null ? 0.0
            : mc.player.getDeltaMovement().x * dirX + mc.player.getDeltaMovement().z * dirZ;
        return new Metrics(
            gapBlocks,
            distanceBlocks,
            Math.sqrt(dx * dx + dz * dz),
            progress,
            len - progress,
            lateral,
            velocityAlong);
    }

    private static String phase(Minecraft mc) {
        if (mc.player == null) {
            return "no-player";
        }
        if (mc.player.isInWater()) {
            return "water";
        }
        return mc.player.onGround() ? "ground" : "air";
    }

    private static String keyState(Minecraft mc) {
        return (mc.options.keyUp.isDown() ? "W" : "-")
            + (mc.options.keyDown.isDown() ? "S" : "-")
            + (mc.options.keyLeft.isDown() ? "A" : "-")
            + (mc.options.keyRight.isDown() ? "D" : "-")
            + (mc.options.keyJump.isDown() ? "J" : "-")
            + (mc.options.keySprint.isDown() ? "R" : "-");
    }

    private static Node.MoveType nextMove(List<Node> path, int pursuitSegment) {
        int index = Math.min(path.size() - 1, pursuitSegment + 2);
        return path.get(index).moveType;
    }

    private record Metrics(
        int gapBlocks,
        int distanceBlocks,
        double horizontalDistance,
        double progress,
        double remaining,
        double lateral,
        double velocityAlong
    ) {
    }
}
