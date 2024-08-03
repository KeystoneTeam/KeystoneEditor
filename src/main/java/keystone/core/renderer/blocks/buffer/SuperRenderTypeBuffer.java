package keystone.core.renderer.blocks.buffer;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;

public class SuperRenderTypeBuffer implements VertexConsumerProvider
{
    static SuperRenderTypeBuffer instance;

    public static SuperRenderTypeBuffer getInstance()
    {
        if (instance == null)
            instance = new SuperRenderTypeBuffer();
        return instance;
    }
    
    VertexConsumerProvider.Immediate consumers;

    public SuperRenderTypeBuffer()
    {
        int i = Runtime.getRuntime().availableProcessors();
        BufferBuilderStorage buffers = new BufferBuilderStorage(i);
        consumers = buffers.getEntityVertexConsumers();
    }
    
    @Override
    public VertexConsumer getBuffer(RenderLayer type)
    {
        return consumers.getBuffer(type);
    }
    
    public void draw()
    {
        RenderSystem.disableCull();
        consumers.draw();
    }

    public void draw(RenderLayer type)
    {
        RenderSystem.disableCull();
        consumers.draw(type);
    }
}