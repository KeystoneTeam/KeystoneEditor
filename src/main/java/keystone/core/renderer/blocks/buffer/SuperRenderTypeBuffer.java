package keystone.core.renderer.blocks.buffer;


import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Util;

import java.util.SortedMap;

public class SuperRenderTypeBuffer implements VertexConsumerProvider
{
    static SuperRenderTypeBuffer instance;

    public static SuperRenderTypeBuffer getInstance()
    {
        if (instance == null)
            instance = new SuperRenderTypeBuffer();
        return instance;
    }
    
    VertexConsumerProvider.Immediate earlyBuffer;
    VertexConsumerProvider.Immediate defaultBuffer;
    VertexConsumerProvider.Immediate lateBuffer;

    public SuperRenderTypeBuffer()
    {
        int i = Runtime.getRuntime().availableProcessors();
        BufferBuilderStorage earlyBuffers = new BufferBuilderStorage(i);
        BufferBuilderStorage defaultBuffers = new BufferBuilderStorage(i);
        BufferBuilderStorage lateBuffers = new BufferBuilderStorage(i);
        
        earlyBuffer = earlyBuffers.getEntityVertexConsumers();
        defaultBuffer = defaultBuffers.getEntityVertexConsumers();
        lateBuffer = lateBuffers.getEntityVertexConsumers();
    }

    public VertexConsumer getEarlyBuffer(RenderLayer type)
    {
        return earlyBuffer.getBuffer(type);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer type)
    {
        return defaultBuffer.getBuffer(type);
    }

    public VertexConsumer getLateBuffer(RenderLayer type)
    {
        return lateBuffer.getBuffer(type);
    }

    public void draw()
    {
        RenderSystem.disableCull();
        earlyBuffer.draw();
        defaultBuffer.draw();
        lateBuffer.draw();
    }

    public void draw(RenderLayer type)
    {
        RenderSystem.disableCull();
        earlyBuffer.draw(type);
        defaultBuffer.draw(type);
        lateBuffer.draw(type);
    }
}