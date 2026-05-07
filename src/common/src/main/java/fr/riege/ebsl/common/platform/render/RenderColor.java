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

    public static RenderColor hsv(float hue, float saturation, float value, float alpha) {
        saturation = clamp(saturation);
        value = clamp(value);
        alpha = clamp(alpha);
        float h = wrap(hue) * 6.0f;
        int section = (int) Math.floor(h);
        float fraction = h - section;
        float p = value * (1.0f - saturation);
        float q = value * (1.0f - fraction * saturation);
        float t = value * (1.0f - (1.0f - fraction) * saturation);
        return switch (Math.floorMod(section, 6)) {
            case 0 -> rgba(value, t, p, alpha);
            case 1 -> rgba(q, value, p, alpha);
            case 2 -> rgba(p, value, t, alpha);
            case 3 -> rgba(p, q, value, alpha);
            case 4 -> rgba(t, p, value, alpha);
            default -> rgba(value, p, q, alpha);
        };
    }

    public RenderColor lerp(RenderColor to, float progress) {
        RenderColor end = to != null ? to : this;
        float t = clamp(progress);
        return rgba(
            r + (end.r - r) * t,
            g + (end.g - g) * t,
            b + (end.b - b) * t,
            a + (end.a - a) * t);
    }

    public RenderColor withAlpha(float alpha) {
        return rgba(r, g, b, alpha);
    }

    private static float clamp(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }

    private static float wrap(float value) {
        float wrapped = value % 1.0f;
        return wrapped < 0.0f ? wrapped + 1.0f : wrapped;
    }
}
