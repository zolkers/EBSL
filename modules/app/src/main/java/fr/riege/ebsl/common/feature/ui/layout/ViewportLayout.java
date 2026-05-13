package fr.riege.ebsl.common.feature.ui.layout;

public record ViewportLayout(UiRect header, UiRect left, UiRect center, UiRect right, UiRect bottom) {
    public static ViewportLayout create(int width, int height) {
        int bottomY = Math.max(UiTheme.HEADER_H + 80, height - UiTheme.BOTTOM_H);
        int rightX = Math.max(UiTheme.LEFT_W + 120, width - UiTheme.RIGHT_W);
        return new ViewportLayout(
            new UiRect(0, 0, width, UiTheme.HEADER_H),
            new UiRect(0, UiTheme.HEADER_H, UiTheme.LEFT_W, bottomY - UiTheme.HEADER_H),
            new UiRect(UiTheme.LEFT_W, UiTheme.HEADER_H, rightX - UiTheme.LEFT_W, bottomY - UiTheme.HEADER_H),
            new UiRect(rightX, UiTheme.HEADER_H, width - rightX, bottomY - UiTheme.HEADER_H),
            new UiRect(0, bottomY, width, height - bottomY));
    }
}
