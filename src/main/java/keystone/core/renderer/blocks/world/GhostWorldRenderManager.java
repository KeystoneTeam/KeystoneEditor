package keystone.core.renderer.blocks.world;

import keystone.core.mixins.interfaces.KeystoneWorldRenderer;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GhostWorldRenderManager
{
    private static final List<GhostWorld> ghostWorlds = new CopyOnWriteArrayList<>();
    
    public static void registerGhostWorld(GhostWorld world)
    {
        ghostWorlds.add(world);
    }
    public static void unregisterGhostWorld(GhostWorld world)
    {
        ghostWorlds.remove(world);
        world.getRenderer().close();
    }
    
    public static void setupFrustum(Vec3d vec3d, Matrix4f matrix4f, Matrix4f matrix4f2)
    {
        for (GhostWorld world : ghostWorlds)
        {
            world.getRenderer().setupFrustum(vec3d, matrix4f, matrix4f2);
        }
    }
    public static void render(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2)
    {
        WorldRenderHelper.WorldRenderContext originalContext = WorldRenderHelper.WorldRenderContext.captureCurrent();
        
        for (GhostWorld world : ghostWorlds)
        {
            originalContext.copy().setupGhostWorld(world).apply();
            world.getRenderer().render(tickCounter, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f, matrix4f2);
        }
        
        originalContext.apply();
    }
    public static void drawEntityOutlinesFramebuffer()
    {
        for (GhostWorld world : ghostWorlds) world.getRenderer().drawEntityOutlinesFramebuffer();
    }
    public static void reload()
    {
        for (GhostWorld world : ghostWorlds) world.getRenderer().reload();
    }
    public static void close()
    {
        for (GhostWorld world : ghostWorlds) world.getRenderer().close();
    }
    public static void cleanUp()
    {
        for (GhostWorld world : ghostWorlds) world.getRenderer().cleanUp();
    }
}
