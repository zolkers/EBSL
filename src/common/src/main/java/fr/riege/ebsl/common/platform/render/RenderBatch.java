package fr.riege.ebsl.common.platform.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record RenderBatch(String id, RenderStage stage, RenderStyle style, int ttlTicks, List<RenderPrimitive> primitives) {
    public static final int PERSISTENT = -1;
    public static final int ONE_FRAME = 1;

    public RenderBatch {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("render batch id required");
        }
        stage = stage != null ? stage : RenderStage.DEBUG;
        style = style != null ? style : RenderStyle.DEFAULT;
        primitives = List.copyOf(primitives != null ? primitives : List.of());
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public Builder toBuilder() {
        return new Builder(id)
            .stage(stage)
            .style(style)
            .ttlTicks(ttlTicks)
            .addAll(primitives);
    }

    public static final class Builder {
        private final String id;
        private RenderStage stage = RenderStage.DEBUG;
        private RenderStyle style = RenderStyle.DEFAULT;
        private int ttlTicks = PERSISTENT;
        private final List<RenderPrimitive> primitives = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder stage(RenderStage stage) {
            this.stage = stage != null ? stage : RenderStage.DEBUG;
            return this;
        }

        public Builder style(RenderStyle style) {
            this.style = style != null ? style : RenderStyle.DEFAULT;
            return this;
        }

        public Builder color(RenderColor color) {
            this.style = style.toBuilder().color(color).build();
            return this;
        }

        public Builder argb(int argb) {
            return color(RenderColor.argb(argb));
        }

        public Builder lineWidth(float lineWidth) {
            this.style = style.toBuilder().lineWidth(lineWidth).build();
            return this;
        }

        public Builder ignoreDepth(boolean ignoreDepth) {
            this.style = style.toBuilder().ignoreDepth(ignoreDepth).build();
            return this;
        }

        public Builder ttlTicks(int ttlTicks) {
            this.ttlTicks = ttlTicks;
            return this;
        }

        public Builder oneFrame() {
            return ttlTicks(ONE_FRAME);
        }

        public Builder persistent() {
            return ttlTicks(PERSISTENT);
        }

        public Builder line(double x1, double y1, double z1,
                            double x2, double y2, double z2) {
            return line(x1, y1, z1, x2, y2, z2, null);
        }

        public Builder line(double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            RenderStyle style) {
            primitives.add(new RenderPrimitive.Line(x1, y1, z1, x2, y2, z2, style));
            return this;
        }

        public Builder filledBlock(int x, int y, int z) {
            return filledBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        }

        public Builder wireBlock(int x, int y, int z) {
            return wireBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        }

        public Builder filledBox(double minX, double minY, double minZ,
                                 double maxX, double maxY, double maxZ) {
            return filledBox(minX, minY, minZ, maxX, maxY, maxZ, null);
        }

        public Builder filledBox(double minX, double minY, double minZ,
                                 double maxX, double maxY, double maxZ,
                                 RenderStyle style) {
            primitives.add(new RenderPrimitive.FilledBox(minX, minY, minZ, maxX, maxY, maxZ, style));
            return this;
        }

        public Builder wireBox(double minX, double minY, double minZ,
                               double maxX, double maxY, double maxZ) {
            return wireBox(minX, minY, minZ, maxX, maxY, maxZ, null);
        }

        public Builder wireBox(double minX, double minY, double minZ,
                               double maxX, double maxY, double maxZ,
                               RenderStyle style) {
            primitives.add(new RenderPrimitive.WireBox(minX, minY, minZ, maxX, maxY, maxZ, style));
            return this;
        }

        public Builder add(RenderPrimitive primitive) {
            if (primitive != null) {
                primitives.add(primitive);
            }
            return this;
        }

        public Builder addAll(List<RenderPrimitive> primitives) {
            if (primitives != null) {
                for (RenderPrimitive primitive : primitives) {
                    add(primitive);
                }
            }
            return this;
        }

        public List<RenderPrimitive> primitives() {
            return Collections.unmodifiableList(primitives);
        }

        public RenderBatch build() {
            return new RenderBatch(id, stage, style, ttlTicks, primitives);
        }

        public RenderBatch submit() {
            RenderBatch batch = build();
            RenderingSystem.submit(batch);
            return batch;
        }

        public RenderBatch drawOnce() {
            oneFrame();
            return submit();
        }
    }
}
