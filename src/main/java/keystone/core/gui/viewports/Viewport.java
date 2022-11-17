package keystone.core.gui.viewports;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class Viewport
{
    public static final int TOP = 0;
    public static final int LEFT = 0;
    public static final int MIDDLE = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 2;

    private final Window window;
    private final float normalizedMinX;
    private final float normalizedMinY;
    private final float normalizedMaxX;
    private final float normalizedMaxY;

    private int minXOffset;
    private int minYOffset;
    private int maxXOffset;
    private int maxYOffset;
    private float scale;

    public Viewport(float normalizedMinX, float normalizedMinY, float normalizedMaxX, float normalizedMaxY)
    {
        this.window = MinecraftClient.getInstance().getWindow();
        this.normalizedMinX = normalizedMinX;
        this.normalizedMinY = normalizedMinY;
        this.normalizedMaxX = normalizedMaxX;
        this.normalizedMaxY = normalizedMaxY;
        this.scale = 1;
    }
    public Viewport clone()
    {
        return new Viewport(normalizedMinX, normalizedMinY, normalizedMaxX, normalizedMaxY).offset(minXOffset, minYOffset, maxXOffset, maxYOffset);
    }

    public Viewport offset(int minXOffset, int minYOffset, int maxXOffset, int maxYOffset)
    {
        this.minXOffset += minXOffset;
        this.minYOffset += minYOffset;
        this.maxXOffset += maxXOffset;
        this.maxYOffset += maxYOffset;
        return this;
    }
    public Viewport scale(float scale)
    {
        this.scale = scale;
        return this;
    }
    public Viewport setHeight(int height)
    {
        float halfNormalizedHeight = height / (float)window.getScaledHeight() / 2.0f;
        float center = (normalizedMinY + normalizedMaxY) / 2.0f;
        return new Viewport(normalizedMinX, center - halfNormalizedHeight, normalizedMaxX, center + halfNormalizedHeight).offset(minXOffset, 0, maxXOffset, 0);
    }
    public Viewport createAspectRatioViewport(float aspectRatio)
    {
        float viewportAspectRatio = getWidth() / (float)getHeight();

        if (viewportAspectRatio >= aspectRatio)
        {
            float halfNormalizedWidth = aspectRatio * getHeight() / window.getScaledWidth() / 2.0f;
            float center = (normalizedMinX + normalizedMaxX) / 2.0f;
            return new Viewport(center - halfNormalizedWidth, normalizedMinY, center + halfNormalizedWidth, normalizedMaxY);
        }
        else
        {
            float halfNormalizedHeight = getWidth() / aspectRatio / window.getScaledHeight() / 2.0f;
            float center = (normalizedMinY + normalizedMaxY) / 2.0f;
            return new Viewport(normalizedMinX, center - halfNormalizedHeight, normalizedMaxX, center + halfNormalizedHeight);
        }
    }

    public int getMinX() { return (int)(normalizedMinX * window.getScaledWidth() / scale) + minXOffset; }
    public int getMinY() { return (int)(normalizedMinY * window.getScaledHeight() / scale) + minYOffset; }
    public int getMaxX() { return (int)(normalizedMaxX * window.getScaledWidth() / scale) + maxXOffset; }
    public int getMaxY() { return (int)(normalizedMaxY * window.getScaledHeight() / scale) + maxYOffset; }
    public int getWidth() { return getMaxX() - getMinX(); }
    public int getHeight() { return getMaxY() - getMinY(); }

    public float getNormalizedMinX() { return normalizedMinX; }
    public float getNormalizedMinY() { return normalizedMinY; }
    public float getNormalizedMaxX() { return normalizedMaxX; }
    public float getNormalizedMaxY() { return normalizedMaxY; }

    public int getMinXOffset() { return minXOffset; }
    public int getMinYOffset() { return minYOffset; }
    public int getMaxXOffset() { return maxXOffset; }
    public int getMaxYOffset() { return maxYOffset; }
    public float getScale() { return scale; }

    @Override
    public String toString()
    {
        return "Viewport[(" + getMinX() + ", " + getMinY() + ") -> (" + getMaxX() + ", " + getMaxY() + ")]";
    }
}
