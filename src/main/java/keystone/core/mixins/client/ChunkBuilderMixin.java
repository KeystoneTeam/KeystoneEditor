package keystone.core.mixins.client;

import keystone.core.mixins.interfaces.KeystoneChunkBuilder;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin implements KeystoneChunkBuilder
{
    @Shadow @Final @Mutable private BlockBufferBuilderPool buffersPool;
    
    @Shadow @Mutable @Final BlockBufferAllocatorStorage buffers;
    
    @Override public BlockBufferBuilderPool keystone_getBuffersPool() { return buffersPool; }
    @Override public void keystone_setBuffersPool(BlockBufferBuilderPool pool) { this.buffersPool = pool; }
    
    @Override
    public BlockBufferAllocatorStorage keystone_getBufferAllocatorStorage()
    {
        return buffers;
    }
    
    @Override
    public void keystone_setBufferAllocatorStorage(BlockBufferAllocatorStorage bufferAllocatorStorage)
    {
        buffers = bufferAllocatorStorage;
    }
}
