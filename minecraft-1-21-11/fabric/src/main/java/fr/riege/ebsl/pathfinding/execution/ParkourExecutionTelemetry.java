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
    private static final long ANALYTICS_INTERVAL_MS = 0;
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
        long now = System.currentTimeMillis();
        Metrics metrics = metrics(path, playerPos, waypoint, pursuitSegment);
        String signature = phase(mc) + "|" + decision + "|" + keyState(mc);
        if (now - lastAnalyticsTime < ANALYTICS_INTERVAL_MS && signature.equals(lastSignature)) {
            return;
        }

        String message = String.format(Locale.ROOT,
            "seg=%d %s gap=%d off=%d dxz=%d,%d dy=%d h=%.2f blockRem=%.2f prog=%.2f rem=%.2f lat=%.2f vAlong=%.3f vel=%.3f,%.3f,%.3f cd=%d allow=%s phase=%s onGround=%s water=%s yaw=%.1f pitch=%.1f next=%s keys=%s pos=%.2f,%.2f,%.2f block=%d,%d,%d takeoff=%d,%d,%d landing=%d,%d,%d path=%s",
            pursuitSegment,
            decision,
            metrics.gapBlocks,
            metrics.distanceBlocks,
            metrics.offsetX,
            metrics.offsetZ,
            metrics.verticalDelta,
            metrics.horizontalDistance,
            metrics.blockRemaining,
            metrics.progress,
            metrics.remaining,
            metrics.lateral,
            metrics.velocityAlong,
            mc.player.getDeltaMovement().x,
            mc.player.getDeltaMovement().y,
            mc.player.getDeltaMovement().z,
            jumpCooldown,
            allowJumps,
            phase(mc),
            mc.player.onGround(),
            mc.player.isInWater(),
            mc.player.getYRot(),
            mc.player.getXRot(),
            nextMove(path, pursuitSegment),
            keyState(mc),
            playerPos.x,
            playerPos.y,
            playerPos.z,
            mc.player.getBlockX(),
            mc.player.getBlockY(),
            mc.player.getBlockZ(),
            metrics.takeoffX,
            metrics.takeoffY,
            metrics.takeoffZ,
            waypoint.position.flooredX(),
            waypoint.position.flooredY(),
            waypoint.position.flooredZ(),
            pathWindow(path, pursuitSegment));

        AnalyticsEventLog.record("parkour", message);
        lastAnalyticsTime = now;
        lastSignature = signature;

        if (PathfinderSettings.instance().showDebug.value() && now - lastChatTime >= CHAT_INTERVAL_MS) {
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
            return new Metrics(
                takeoff.position.flooredX(),
                takeoff.position.flooredY(),
                takeoff.position.flooredZ(),
                0,
                0,
                waypoint.position.flooredY() - takeoff.position.flooredY(),
                0,
                0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0);
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
        int offsetY = waypoint.position.flooredY() - takeoff.position.flooredY();
        int offsetZ = waypoint.position.flooredZ() - takeoff.position.flooredZ();
        int distanceBlocks = ParkourGeometry.distanceBlocks(offsetX, offsetZ);
        int gapBlocks = ParkourGeometry.isDiagonal(offsetX, offsetZ)
            ? ParkourGeometry.diagonalGapBlocks(offsetX, offsetZ)
            : ParkourGeometry.cardinalGapBlocks(offsetX, offsetZ);
        Minecraft mc = Minecraft.getInstance();
        double velocityAlong = mc.player == null ? 0.0
            : mc.player.getDeltaMovement().x * dirX + mc.player.getDeltaMovement().z * dirZ;
        return new Metrics(
            takeoff.position.flooredX(),
            takeoff.position.flooredY(),
            takeoff.position.flooredZ(),
            offsetX,
            offsetZ,
            offsetY,
            gapBlocks,
            distanceBlocks,
            Math.sqrt(dx * dx + dz * dz),
            Math.max(Math.abs(dx), Math.abs(dz)),
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

    private static String pathWindow(List<Node> path, int pursuitSegment) {
        int start = Math.max(0, pursuitSegment - 2);
        int end = Math.min(path.size() - 1, pursuitSegment + 4);
        StringBuilder builder = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (!builder.isEmpty()) {
                builder.append(";");
            }
            Node node = path.get(i);
            if (i == pursuitSegment) {
                builder.append("*");
            }
            builder
                .append(i)
                .append(":")
                .append(node.moveType)
                .append("@")
                .append(node.position.flooredX())
                .append(",")
                .append(node.position.flooredY())
                .append(",")
                .append(node.position.flooredZ());
        }
        return builder.toString();
    }

    private record Metrics(
        int takeoffX,
        int takeoffY,
        int takeoffZ,
        int offsetX,
        int offsetZ,
        int verticalDelta,
        int gapBlocks,
        int distanceBlocks,
        double horizontalDistance,
        double blockRemaining,
        double progress,
        double remaining,
        double lateral,
        double velocityAlong
    ) {
    }
}
