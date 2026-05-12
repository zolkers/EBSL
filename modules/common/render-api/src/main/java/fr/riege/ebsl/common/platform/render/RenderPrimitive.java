package fr.riege.ebsl.common.platform.render;

public sealed interface RenderPrimitive permits RenderPrimitive.Line, RenderPrimitive.FilledBox, RenderPrimitive.WireBox {
    void render(RenderHandle handle, RenderStyle fallbackStyle);

    void render(WorldRenderSession session, RenderStyle fallbackStyle);

    RenderStyle effectiveStyle(RenderStyle fallbackStyle);

    record Line(double x1, double y1, double z1,
                double x2, double y2, double z2,
                RenderStyle style) implements RenderPrimitive {
        @Override
        public void render(RenderHandle handle, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            WorldRender.builder(handle)
                .paint(s.paint())
                .lineWidth(s.lineWidth())
                .ignoreDepth(s.ignoreDepth())
                .line(x1, y1, z1, x2, y2, z2);
        }

        @Override
        public void render(WorldRenderSession session, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            session.paint(s.paint())
                .lineWidth(s.lineWidth())
                .line(x1, y1, z1, x2, y2, z2);
        }

        @Override
        public RenderStyle effectiveStyle(RenderStyle fallbackStyle) {
            return styleOrDefault(style, fallbackStyle);
        }
    }

    record FilledBox(double minX, double minY, double minZ,
                     double maxX, double maxY, double maxZ,
                     RenderStyle style) implements RenderPrimitive {
        @Override
        public void render(RenderHandle handle, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            WorldRender.builder(handle)
                .paint(s.paint())
                .ignoreDepth(s.ignoreDepth())
                .filledBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public void render(WorldRenderSession session, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            session.paint(s.paint())
                .filledBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public RenderStyle effectiveStyle(RenderStyle fallbackStyle) {
            return styleOrDefault(style, fallbackStyle);
        }
    }

    record WireBox(double minX, double minY, double minZ,
                   double maxX, double maxY, double maxZ,
                   RenderStyle style) implements RenderPrimitive {
        @Override
        public void render(RenderHandle handle, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            WorldRender.builder(handle)
                .paint(s.paint())
                .lineWidth(s.lineWidth())
                .ignoreDepth(s.ignoreDepth())
                .wireBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public void render(WorldRenderSession session, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            session.paint(s.paint())
                .lineWidth(s.lineWidth())
                .wireBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public RenderStyle effectiveStyle(RenderStyle fallbackStyle) {
            return styleOrDefault(style, fallbackStyle);
        }
    }

    private static RenderStyle styleOrDefault(RenderStyle style, RenderStyle fallbackStyle) {
        if (style != null) {
            return style;
        }
        return fallbackStyle != null ? fallbackStyle : RenderStyle.DEFAULT;
    }
}
