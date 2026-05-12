package fr.riege.ebsl.common.platform.render;

public record RenderStyle(RenderPaint paint, float lineWidth, boolean ignoreDepth) {
    public static final RenderStyle DEFAULT = builder().build();

    public static Builder builder() {
        return new Builder();
    }

    public RenderStyle {
        paint = paint != null ? paint : RenderPaint.SOLID_WHITE;
        lineWidth = Math.max(0.1f, lineWidth);
    }

    public RenderStyle(RenderColor color, float lineWidth, boolean ignoreDepth) {
        this(RenderPaint.solid(color), lineWidth, ignoreDepth);
    }

    public RenderColor color() {
        return paint.baseColor();
    }

    public Builder toBuilder() {
        return builder()
            .paint(paint)
            .lineWidth(lineWidth)
            .ignoreDepth(ignoreDepth);
    }

    public static final class Builder {
        private RenderPaint paint = RenderPaint.SOLID_WHITE;
        private float lineWidth = 1.0f;
        private boolean ignoreDepth;

        private Builder() {
        }

        public Builder paint(RenderPaint paint) {
            this.paint = paint != null ? paint : RenderPaint.SOLID_WHITE;
            return this;
        }

        public Builder color(RenderColor color) {
            return paint(RenderPaint.solid(color));
        }

        public Builder argb(int argb) {
            return color(RenderColor.argb(argb));
        }

        public Builder gradient(RenderColor from, RenderColor to) {
            return paint(RenderPaint.gradient(from, to));
        }

        public Builder gradientArgb(int from, int to) {
            return gradient(RenderColor.argb(from), RenderColor.argb(to));
        }

        public Builder rainbow() {
            return paint(RenderPaint.rainbow());
        }

        public Builder rainbow(float alpha) {
            return paint(RenderPaint.rainbow(alpha));
        }

        public Builder rainbow(float alpha, float saturation, float brightness, float cycles, float speed) {
            return paint(RenderPaint.rainbow(alpha, saturation, brightness, cycles, speed));
        }

        public Builder lineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Builder ignoreDepth(boolean ignoreDepth) {
            this.ignoreDepth = ignoreDepth;
            return this;
        }

        public Builder throughWalls() {
            return ignoreDepth(true);
        }

        public Builder depthTested() {
            return ignoreDepth(false);
        }

        public RenderStyle build() {
            return new RenderStyle(paint, lineWidth, ignoreDepth);
        }
    }
}
