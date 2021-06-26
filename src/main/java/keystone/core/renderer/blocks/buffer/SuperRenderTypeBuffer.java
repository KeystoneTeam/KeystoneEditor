package keystone.core.renderer.blocks.buffer;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.Util;

import java.util.SortedMap;

public class SuperRenderTypeBuffer implements IRenderTypeBuffer
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

    public IVertexBuilder getEarlyBuffer(RenderType type)
    {
        return earlyBuffer.getBuffer(type);
    }

    @Override
    public IVertexBuilder getBuffer(RenderType type)
    {
        return defaultBuffer.getBuffer(type);
    }

    public IVertexBuilder getLateBuffer(RenderType type)
    {
        return lateBuffer.getBuffer(type);
    }

    public void endBatch()
    {
        RenderSystem.disableCull();
        earlyBuffer.endBatch();
        defaultBuffer.endBatch();
        lateBuffer.endBatch();
    }

    public void endBatch(RenderType type)
    {
        RenderSystem.disableCull();
        earlyBuffer.endBatch(type);
        defaultBuffer.endBatch(type);
        lateBuffer.endBatch(type);
    }

    private static class SuperRenderTypeBufferPhase extends IRenderTypeBuffer.Impl
    {
        static final RegionRenderCacheBuilder blockBuilders = new RegionRenderCacheBuilder();

        static final SortedMap<RenderType, BufferBuilder> createEntityBuilders()
        {
            return Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) ->
            {
                map.put(Atlases.solidBlockSheet(), blockBuilders.builder(RenderType.solid()));
                map.put(Atlases.cutoutBlockSheet(), blockBuilders.builder(RenderType.cutout()));
                map.put(Atlases.bannerSheet(), blockBuilders.builder(RenderType.cutoutMipped()));
                map.put(Atlases.translucentCullBlockSheet(), blockBuilders.builder(RenderType.translucent()));
                put(map, Atlases.shieldSheet());
                put(map, Atlases.bedSheet());
                put(map, Atlases.shulkerBoxSheet());
                put(map, Atlases.signSheet());
                put(map, Atlases.chestSheet());
                put(map, RenderType.translucentNoCrumbling());
                put(map, RenderType.armorGlint());
                put(map, RenderType.armorEntityGlint());
                put(map, RenderType.glint());
                put(map, RenderType.glintDirect());
                put(map, RenderType.glintTranslucent());
                put(map, RenderType.entityGlint());
                put(map, RenderType.entityGlintDirect());
                put(map, RenderType.waterMask());
                ModelBakery.DESTROY_TYPES.forEach((renderType) -> {
                    put(map, renderType);
                });
            });
        }

        private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, RenderType type)
        {
            map.put(type, new BufferBuilder(type.bufferSize()));
        }

        protected SuperRenderTypeBufferPhase()
        {
            super(new BufferBuilder(256), createEntityBuilders());
        }

    }
}