package keystone.core.mixins.interfaces;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;

public interface KeystoneGameRenderer
{
    boolean keystone_getRenderHand();
    void keystone_setRenderHand(boolean renderHand);
    
    void keystone_setLightmapTextureManager(LightmapTextureManager lightmapTextureManager);
    void keystone_setCamera(Camera camera);
}
