package fr.riege.ebsl.common.platform.render;

public record RenderStyle(RenderColor color, float lineWidth, boolean ignoreDepth) {
    public static final RenderStyle DEFAULT = builder().build();

    public static Builder builder() {
        return new Builder();
    }

    public RenderStyle {
        color = color != null ? color : RenderColor.WHITE;
        lineWidth = Math.max(0.1f, lineWidth);
    }

    public Builder toBuilder() {
        return builder()
            .color(color)
            .lineWidth(lineWidth)
            .ignoreDepth(ignoreDepth);
    }

    public static final class Builder {
        private RenderColor color = RenderColor.WHITE;
        private float lineWidth = 1.0f;
        private boolean ignoreDepth;

        private Builder() {
        }

        public Builder color(RenderColor color) {
            this.color = color != null ? color : RenderColor.WHITE;
            return this;
        }

        public Builder argb(int argb) {
            return color(RenderColor.argb(argb));
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
            return new RenderStyle(color, lineWidth, ignoreDepth);
        }
    }
}
