package fr.riege.ebsl.common.platform.render;

/**
 * Emits low-level world rendering primitives for the active frame.
 *
 * <p>Higher-level render APIs use this handle to batch lines and triangles relative to the current camera.</p>
 */
@SuppressWarnings("java:S107")
public interface RenderHandle {

    /**
     * Begins a line batch using the supplied color.
 *
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    void beginLines(float r, float g, float b, float a);

    /**
     * Emits one line segment into the active line batch.
 *
     * @param x1 the x1 value
     * @param y1 the y1 value
     * @param z1 the z1 value
     * @param x2 the x2 value
     * @param y2 the y2 value
     * @param z2 the z2 value
     * @param lineWidth the line width value
     */
    void emitLine(double x1, double y1, double z1,
                  double x2, double y2, double z2,
                  float lineWidth);

    /**
     * Emits one line segment into the active line batch.
 *
     * @param x1 the x1 value
     * @param y1 the y1 value
     * @param z1 the z1 value
     * @param x2 the x2 value
     * @param y2 the y2 value
     * @param z2 the z2 value
     * @param lineWidth the line width value
     * @param from the first value or starting position
     * @param to the second value or ending position
     */
    default void emitLine(double x1, double y1, double z1,
                          double x2, double y2, double z2,
                          float lineWidth,
                          RenderColor from,
                          RenderColor to) {
        emitLine(x1, y1, z1, x2, y2, z2, lineWidth);
    }

    /**
     * Begins a triangle batch using the supplied color.
 *
     * @param r the r value
     * @param g the g value
     * @param b the b value
     * @param a the a value
     */
    void beginTriangles(float r, float g, float b, float a);

    /**
     * Emits one triangle into the active triangle batch.
 *
     * @param x1 the x1 value
     * @param y1 the y1 value
     * @param z1 the z1 value
     * @param x2 the x2 value
     * @param y2 the y2 value
     * @param z2 the z2 value
     * @param x3 the x3 value
     * @param y3 the y3 value
     * @param z3 the z3 value
     */
    void emitTriangle(double x1, double y1, double z1,
                      double x2, double y2, double z2,
                      double x3, double y3, double z3);

    /**
     * Emits one triangle into the active triangle batch.
 *
     * @param x1 the x1 value
     * @param y1 the y1 value
     * @param z1 the z1 value
     * @param x2 the x2 value
     * @param y2 the y2 value
     * @param z2 the z2 value
     * @param x3 the x3 value
     * @param y3 the y3 value
     * @param z3 the z3 value
     * @param a the a value
     * @param b the b value
     * @param c the c value
     */
    default void emitTriangle(double x1, double y1, double z1,
                              double x2, double y2, double z2,
                              double x3, double y3, double z3,
                              RenderColor a,
                              RenderColor b,
                              RenderColor c) {
        emitTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }

    /**
     * Ends the active primitive batch.
 *
     * @param ignoreDepth whether depth testing should be ignored for emitted primitives
     */
    void end(boolean ignoreDepth);

    /**
     * Returns the camera x coordinate for the active render frame.
 *
     * @return the value defined by this contract
     */
    double cameraX();

    /**
     * Returns the camera y coordinate for the active render frame.
 *
     * @return the value defined by this contract
     */
    double cameraY();

    /**
     * Returns the camera z coordinate for the active render frame.
 *
     * @return the value defined by this contract
     */
    double cameraZ();
}
