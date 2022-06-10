package keystone.core.gui.viewports;

import keystone.core.KeystoneConfig;

public class ScreenViewports
{
    private static Viewport[][] viewports;
    static { refreshViewports(); }
    public static void refreshViewports()
    {
        float top = KeystoneConfig.viewportTopBorder;
        float bottom = 1 - KeystoneConfig.viewportBottomBorder;
        float left = KeystoneConfig.viewportLeftBorder;
        float right = 1 - KeystoneConfig.viewportRightBorder;
        int padding = KeystoneConfig.viewportPadding;

        viewports = new Viewport[][]
        {
            { new Viewport(0, 0, left, top), new Viewport(left, 0, right, top).offset(padding, 0, -padding, 0), new Viewport(right, 0, 1, top) },
            { new Viewport(0, top, left, bottom).offset(0, padding, 0, -padding), new Viewport(left, top, right, bottom).offset(padding, padding, -padding, -padding), new Viewport(right, top, 1, bottom).offset(0, padding, 0, -padding) },
            { new Viewport(0, bottom, left, 1), new Viewport(left, bottom, right, 1).offset(padding, 0, -padding, 0), new Viewport(right, bottom, 1, 1) }
        };
    }

    public static Viewport getViewport(int column, int row)
    {
        return viewports[column][row].clone();
    }
    public static Viewport getViewport(int minColumn, int minRow, int maxColumn, int maxRow)
    {
        Viewport min = getViewport(minColumn, minRow);
        Viewport max = getViewport(maxColumn, maxRow);

        float minX = Math.min(min.getNormalizedMinX(), max.getNormalizedMinX());
        float minY = Math.min(min.getNormalizedMinY(), max.getNormalizedMinY());
        float maxX = Math.max(min.getNormalizedMaxX(), max.getNormalizedMaxX());
        float maxY = Math.max(min.getNormalizedMaxY(), max.getNormalizedMaxY());

        int minXOffset = Math.max(min.getMinXOffset(), max.getMinXOffset());
        int minYOffset = Math.max(min.getMinYOffset(), max.getMinYOffset());
        int maxXOffset = Math.min(min.getMaxXOffset(), max.getMaxXOffset());
        int maxYOffset = Math.min(min.getMaxYOffset(), max.getMaxYOffset());

        return new Viewport(minX, minY, maxX, maxY).offset(minXOffset, minYOffset, maxXOffset, maxYOffset);
    }
}
