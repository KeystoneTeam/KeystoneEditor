package keystone.core.renderer.blocks.buffer;


import java.util.SortedMap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.Util;

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

    public void finish()
    {
        RenderSystem.disableCull();
        earlyBuffer.finish();
        defaultBuffer.finish();
        lateBuffer.finish();
    }

    public void finish(RenderType type)
    {
        RenderSystem.disableCull();
        earlyBuffer.finish(type);
        defaultBuffer.finish(type);
        lateBuffer.finish(type);
    }

    private static class SuperRenderTypeBufferPhase extends IRenderTypeBuffer.Impl
    {
        static final RegionRenderCacheBuilder blockBuilders = new RegionRenderCacheBuilder();

        static final SortedMap<RenderType, BufferBuilder> createEntityBuilders()
        {
            return Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) ->
            {
                map.put(Atlases.getSolidBlockType(), blockBuilders.getBuilder(RenderType.getSolid()));
                map.put(Atlases.getCutoutBlockType(), blockBuilders.getBuilder(RenderType.getCutout()));
                map.put(Atlases.getBannerType(), blockBuilders.getBuilder(RenderType.getCutoutMipped()));
                map.put(Atlases.getTranslucentCullBlockType(), blockBuilders.getBuilder(RenderType.getTranslucent()));
                put(map, Atlases.getShieldType());
                put(map, Atlases.getBedType());
                put(map, Atlases.getShulkerBoxType());
                put(map, Atlases.getSignType());
                put(map, Atlases.getChestType());
                put(map, RenderType.getTranslucentNoCrumbling());
                put(map, RenderType.getArmorGlint());
                put(map, RenderType.getArmorEntityGlint());
                put(map, RenderType.getGlint());
                put(map, RenderType.getGlintDirect());
                put(map, RenderType.getGlintTranslucent());
                put(map, RenderType.getEntityGlint());
                put(map, RenderType.getEntityGlintDirect());
                put(map, RenderType.getWaterMask());
                ModelBakery.DESTROY_RENDER_TYPES.forEach((renderType) -> {
                    put(map, renderType);
                });
            });
        }

        private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, RenderType type)
        {
            map.put(type, new BufferBuilder(type.getBufferSize()));
        }

        protected SuperRenderTypeBufferPhase()
        {
            super(new BufferBuilder(256), createEntityBuilders());
        }

    }
}