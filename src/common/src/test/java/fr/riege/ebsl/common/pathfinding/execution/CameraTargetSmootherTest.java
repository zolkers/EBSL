package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CameraTargetSmootherTest {
    @Test
    void ignoresSmallHeightNoise() {
        CameraTargetSmoother smoother = new CameraTargetSmoother();
        Vec3d eye = new Vec3d(0.0, 65.62, 0.0);
        smoother.reset(66.0);

        Vec3d result = smoother.smooth(new Vec3d(4.0, 66.08, 4.0), eye);

        assertEquals(66.0, result.y(), 0.03);
    }

    @Test
    void movesTowardRealHeightChangeWithoutJumping() {
        CameraTargetSmoother smoother = new CameraTargetSmoother();
        Vec3d eye = new Vec3d(0.0, 65.62, 0.0);
        smoother.reset(66.0);

        Vec3d first = smoother.smooth(new Vec3d(4.0, 69.0, 4.0), eye);
        assertTrue(first.y() > 66.0);
        assertTrue(first.y() < 66.25);

        Vec3d current = first;
        for (int i = 0; i < 40; i++) {
            current = smoother.smooth(new Vec3d(4.0, 69.0, 4.0), eye);
        }
        assertTrue(current.y() > 68.0);
        assertTrue(current.y() <= 69.0);
    }

    @Test
    void clampsHeightAroundEye() {
        CameraTargetSmoother smoother = new CameraTargetSmoother();
        Vec3d eye = new Vec3d(0.0, 65.62, 0.0);
        smoother.reset(66.0);

        Vec3d current = null;
        for (int i = 0; i < 80; i++) {
            current = smoother.smooth(new Vec3d(4.0, 100.0, 4.0), eye);
        }

        assertTrue(current.y() <= 70.62);
    }
}
