package fr.riege.ebsl.common.feature.ui.imgui;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;

public final class EbslNodeCategoryColors {
    private EbslNodeCategoryColors() {
    }

    public static int body(EbslNodeCategory category) {
        return switch (category) {
            case FLOW -> 0xFF203049;
            case CONTROL -> 0xFF39294C;
            case DATA -> 0xFF263A34;
            case WORLD -> 0xFF45351F;
            case PLAYER -> 0xFF2A4052;
            case SENSOR -> 0xFF29433E;
            case UTILITY -> 0xFF303742;
        };
    }

    public static int header(EbslNodeCategory category) {
        return switch (category) {
            case FLOW -> 0xFF2E5E9E;
            case CONTROL -> 0xFF7249A8;
            case DATA -> 0xFF3D7E61;
            case WORLD -> 0xFF9A7434;
            case PLAYER -> 0xFF3D82A8;
            case SENSOR -> 0xFF438C81;
            case UTILITY -> 0xFF566272;
        };
    }

    public static int headerHovered(EbslNodeCategory category) {
        return brighten(header(category), 34);
    }

    public static int text(EbslNodeCategory category) {
        return switch (category) {
            case FLOW -> 0xFFD8E9FF;
            case CONTROL -> 0xFFE9DCFF;
            case DATA -> 0xFFD9F5E8;
            case WORLD -> 0xFFFFE7C3;
            case PLAYER -> 0xFFD7F1FF;
            case SENSOR -> 0xFFD7F8F2;
            case UTILITY -> 0xFFE5ECF5;
        };
    }

    private static int brighten(int argb, int amount) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.clamp(((argb >>> 16) & 0xFF) + amount, 0, 255);
        int g = Math.clamp(((argb >>> 8) & 0xFF) + amount, 0, 255);
        int b = Math.clamp((argb & 0xFF) + amount, 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
