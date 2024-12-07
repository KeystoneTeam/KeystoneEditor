package keystone.core.mixins.interfaces;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;

public interface KeystoneMinecraftClient
{
    void keystone_setWorldRenderer(WorldRenderer worldRenderer);
    void keystone_setBufferBuilders(BufferBuilderStorage bufferBuilders);
}
