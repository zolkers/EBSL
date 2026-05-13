package fr.riege.ebsl.common.platform.render;

/**
 * Defines the contract for {@code RenderPaint} implementations.
 */
public sealed interface RenderPaint permits RenderPaint.Solid, RenderPaint.Gradient, RenderPaint.Rainbow {
    RenderPaint SOLID_WHITE = solid(RenderColor.WHITE);

    RenderColor colorAt(float progress);

    default RenderColor baseColor() {
        return colorAt(0.0f);
    }

    static RenderPaint solid(RenderColor color) {
        return new Solid(color != null ? color : RenderColor.WHITE);
    }

    static RenderPaint gradient(RenderColor from, RenderColor to) {
        return new Gradient(
            from != null ? from : RenderColor.WHITE,
            to != null ? to : RenderColor.WHITE);
    }

    static RenderPaint rainbow() {
        return rainbow(0.95f, 1.0f, 1.0f, 0.82f, 0.18f);
    }

    static RenderPaint rainbow(float alpha) {
        return rainbow(alpha, 1.0f, 1.0f, 0.82f, 0.18f);
    }

    static RenderPaint rainbow(float alpha, float saturation, float brightness, float cycles, float speed) {
        return new Rainbow(
            Math.clamp(alpha, 0.0f, 1.0f),
            Math.clamp(saturation, 0.0f, 1.0f),
            Math.clamp(brightness, 0.0f, 1.0f),
            Math.max(0.01f, cycles),
            speed);
    }

    record Solid(RenderColor color) implements RenderPaint {
        public Solid {
            color = color != null ? color : RenderColor.WHITE;
        }

        @Override
        public RenderColor colorAt(float progress) {
            return color;
        }
    }

    record Gradient(RenderColor from, RenderColor to) implements RenderPaint {
        public Gradient {
            from = from != null ? from : RenderColor.WHITE;
            to = to != null ? to : RenderColor.WHITE;
        }

        @Override
        public RenderColor colorAt(float progress) {
            return from.lerp(to, progress);
        }
    }

    record Rainbow(float alpha, float saturation, float brightness, float cycles, float speed) implements RenderPaint {
        public Rainbow {
            alpha = Math.clamp(alpha, 0.0f, 1.0f);
            saturation = Math.clamp(saturation, 0.0f, 1.0f);
            brightness = Math.clamp(brightness, 0.0f, 1.0f);
            cycles = Math.max(0.01f, cycles);
        }

        @Override
        public RenderColor colorAt(float progress) {
            float seconds = System.nanoTime() / 1_000_000_000.0f;
            return RenderColor.hsv(progress * cycles + seconds * speed, saturation, brightness, alpha);
        }
    }
}
