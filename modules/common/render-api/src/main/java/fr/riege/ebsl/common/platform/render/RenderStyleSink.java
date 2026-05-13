package fr.riege.ebsl.common.platform.render;

public interface RenderStyleSink<T extends RenderStyleSink<T>> {
    T paint(RenderPaint paint);

    T lineWidth(float lineWidth);

    T ignoreDepth(boolean ignoreDepth);

    default T color(RenderColor color) {
        return paint(RenderPaint.solid(color));
    }

    default T argb(int argb) {
        return color(RenderColor.argb(argb));
    }

    default T gradient(RenderColor from, RenderColor to) {
        return paint(RenderPaint.gradient(from, to));
    }

    default T gradientArgb(int from, int to) {
        return gradient(RenderColor.argb(from), RenderColor.argb(to));
    }

    default T rainbow() {
        return paint(RenderPaint.rainbow());
    }

    default T rainbow(float alpha) {
        return paint(RenderPaint.rainbow(alpha));
    }
}
