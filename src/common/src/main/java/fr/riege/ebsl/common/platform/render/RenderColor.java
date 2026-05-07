package fr.riege.ebsl.common.platform.render;

public record RenderColor(float r, float g, float b, float a) {
    public static final RenderColor WHITE = rgba(1.0f, 1.0f, 1.0f, 1.0f);

    public static RenderColor rgba(float r, float g, float b, float a) {
        return new RenderColor(clamp(r), clamp(g), clamp(b), clamp(a));
    }

    public static RenderColor argb(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        return rgba(r, g, b, a);
    }

    public RenderColor withAlpha(float alpha) {
        return rgba(r, g, b, alpha);
    }

    private static float clamp(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }
}
