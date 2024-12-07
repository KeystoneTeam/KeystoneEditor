package keystone.core.mixins.interfaces;

import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;

public interface KeystoneChunkBuilder
{
    BlockBufferBuilderPool keystone_getBuffersPool();
    void keystone_setBuffersPool(BlockBufferBuilderPool buffersPool);
    
    BlockBufferAllocatorStorage keystone_getBufferAllocatorStorage();
    void keystone_setBufferAllocatorStorage(BlockBufferAllocatorStorage bufferAllocatorStorage);
}
