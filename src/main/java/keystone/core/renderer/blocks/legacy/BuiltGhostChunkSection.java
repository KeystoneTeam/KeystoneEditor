package keystone.core.renderer.blocks.legacy;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import keystone.core.renderer.blocks.legacy.buffer.SuperByteBuffer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuiltGhostChunkSection
{
    public final Map<RenderLayer, SuperByteBuffer> buffers;
    public final List<BlockEntity> blockEntities;
    
    BuiltGhostChunkSection()
    {
        this.buffers = new Reference2ObjectArrayMap<>();
        this.blockEntities = new ArrayList<>();
    }
}
