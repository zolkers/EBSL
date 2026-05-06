package fr.riege.ebsl.common.event;

// viewMatrix and projMatrix are column-major float[16]
public record RenderWorldEvent(float[] viewMatrix, float[] projMatrix, float tickDelta,
                                double camX, double camY, double camZ) {}
