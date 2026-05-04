package fr.riege.ebsl.common.event;

// action: 0=release, 1=press, 2=repeat — GLFW constants
public record KeyPressEvent(int keyCode, int action, int modifiers) {}
