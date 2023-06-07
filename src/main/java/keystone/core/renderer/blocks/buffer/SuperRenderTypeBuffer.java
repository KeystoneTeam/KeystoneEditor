package keystone.core.renderer.blocks.buffer;


import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.model.ModelLoader;
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

    SuperRenderTypeBufferPhase earlyBuffer;
    SuperRenderTypeBufferPhase defaultBuffer;
    SuperRenderTypeBufferPhase lateBuffer;

    public SuperRenderTypeBuffer()
    {
        earlyBuffer = new SuperRenderTypeBufferPhase();
        defaultBuffer = new SuperRenderTypeBufferPhase();
        lateBuffer = new SuperRenderTypeBufferPhase();
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

    private static class SuperRenderTypeBufferPhase extends VertexConsumerProvider.Immediate
    {
        static final BlockBufferBuilderStorage blockBuilders = new BlockBufferBuilderStorage();

        static SortedMap<RenderLayer, BufferBuilder> createEntityBuilders()
        {
            return Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) ->
            {
                map.put(TexturedRenderLayers.getEntitySolid(), blockBuilders.get(RenderLayer.getSolid()));
                map.put(TexturedRenderLayers.getEntityCutout(), blockBuilders.get(RenderLayer.getCutout()));
                map.put(TexturedRenderLayers.getBannerPatterns(), blockBuilders.get(RenderLayer.getCutoutMipped()));
                map.put(TexturedRenderLayers.getEntityTranslucentCull(), blockBuilders.get(RenderLayer.getTranslucent()));
                put(map, TexturedRenderLayers.getShieldPatterns());
                put(map, TexturedRenderLayers.getBeds());
                put(map, TexturedRenderLayers.getShulkerBoxes());
                put(map, TexturedRenderLayers.getSign());
                put(map, TexturedRenderLayers.getChest());
                put(map, RenderLayer.getTranslucentNoCrumbling());
                put(map, RenderLayer.getArmorGlint());
                put(map, RenderLayer.getArmorEntityGlint());
                put(map, RenderLayer.getGlint());
                put(map, RenderLayer.getDirectGlint());
                put(map, RenderLayer.getGlintTranslucent());
                put(map, RenderLayer.getEntityGlint());
                put(map, RenderLayer.getDirectEntityGlint());
                put(map, RenderLayer.getWaterMask());
                ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach((RenderLayer) -> put(map, RenderLayer));
            });
        }

        private static void put(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> map, RenderLayer type)
        {
            map.put(type, new BufferBuilder(type.getExpectedBufferSize()));
        }

        protected SuperRenderTypeBufferPhase()
        {
            super(new BufferBuilder(256), createEntityBuilders());
        }

    }
}