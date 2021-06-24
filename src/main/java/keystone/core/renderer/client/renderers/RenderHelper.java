package keystone.core.renderer.client.renderers;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderHelper
{
    public static final int TRIANGLES = GL11.GL_TRIANGLES;
    public static final int QUADS = GL11.GL_QUADS;
    public static final int LINES = GL11.GL_LINES;
    public static final int LINE_LOOP = GL11.GL_LINE_LOOP;
    public static final int POINTS = GL11.GL_POINTS;

    public static void beforeRender()
    {
        enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        lineWidth2();
        disableTexture();
        GlStateManager._disableCull();
    }

    public static void afterRender()
    {
        polygonModeFill();
        GlStateManager._enableCull();
        enableTexture();
        enableDepthTest();
    }

    public static void beforeRenderFont(OffsetPoint offsetPoint)
    {
        GlStateManager._pushMatrix();
        polygonModeFill();
        GlStateManager._translated(offsetPoint.getX(), offsetPoint.getY() + 0.002D, offsetPoint.getZ());
        GlStateManager._normal3f(0.0F, 1.0F, 0.0F);
        GlStateManager._rotatef(0.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager._rotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager._scalef(-0.0175F, -0.0175F, 0.0175F);
        enableTexture();
        enableBlend();
        GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        depthMaskTrue();
    }

    public static void afterRenderFont()
    {
        disableTexture();
        disableBlend();
        GlStateManager._popMatrix();
        enableDepthTest();
    }

    public static void disableLighting()
    {
        GlStateManager._disableLighting();
    }

    public static void disableDepthTest()
    {
        GlStateManager._disableDepthTest();
    }

    public static void enableDepthTest()
    {
        GlStateManager._enableDepthTest();
    }

    public static void disableFog()
    {
        GlStateManager._disableFog();
    }

    public static void disableBlend()
    {
        GlStateManager._disableBlend();
    }

    public static void enableBlend()
    {
        GlStateManager._enableBlend();
    }

    public static void disableAlphaTest()
    {
        GlStateManager._disableAlphaTest();
    }

    public static void enableAlphaTest()
    {
        GlStateManager._enableAlphaTest();
    }

    public static void disableTexture()
    {
        GlStateManager._disableTexture();
    }

    public static void enableTexture()
    {
        GlStateManager._enableTexture();
    }

    public static void shadeModelSmooth()
    {
        GlStateManager._shadeModel(GL11.GL_SMOOTH);
    }

    public static void shadeModelFlat()
    {
        GlStateManager._shadeModel(GL11.GL_FLAT);
    }

    public static void enablePointSmooth()
    {
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
    }

    public static void lineWidth2()
    {
        GlStateManager._lineWidth(2f);
    }

    public static void polygonModeLine()
    {
        GlStateManager._polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }
    public static void polygonModeFill()
    {
        GlStateManager._polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public static void enableCull()
    {
        GlStateManager._enableCull();
    }
    public static void disableCull()
    {
        GlStateManager._disableCull();
    }

    public static void polygonOffsetMinusOne()
    {
        GlStateManager._polygonOffset(-1.f, -1.f);
    }

    public static void enablePolygonOffsetLine()
    {
        GlStateManager._enableLineOffset();
    }

    public static void depthMaskTrue()
    {
        GlStateManager._depthMask(true);
    }

    public static void pointSize5()
    {
        GL11.glPointSize(5);
    }

    public static void blendFuncGui()
    {
        GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
    }

    public static void depthFuncAlways()
    {
        GlStateManager._depthFunc(GL11.GL_ALWAYS);
    }

    public static void depthFuncLessEqual()
    {
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
    }
}
