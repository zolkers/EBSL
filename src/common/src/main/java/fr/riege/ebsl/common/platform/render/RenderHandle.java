package fr.riege.ebsl.common.platform.render;

public interface RenderHandle {

    void beginLines(float r, float g, float b, float a);

    void emitLine(double x1, double y1, double z1,
                  double x2, double y2, double z2,
                  float lineWidth);

    void beginTriangles(float r, float g, float b, float a);

    void emitTriangle(double x1, double y1, double z1,
                      double x2, double y2, double z2,
                      double x3, double y3, double z3);

    void end(boolean ignoreDepth);

    double cameraX();

    double cameraY();

    double cameraZ();
}
