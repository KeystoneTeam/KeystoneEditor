package keystone.core.mixins.interfaces;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.joml.Matrix4f;

public interface KeystoneWorldRenderer
{
    ObjectArrayList<ChunkBuilder.BuiltChunk> keystone_getBuiltChunks();
    void keystone_setBuiltChunks(ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks);
    
    PostEffectProcessor keystone_getTransparencyPostProcessor();
    void keystone_setTransparencyPostProcessor(PostEffectProcessor transparencyPostProcessor);
    
    Frustum keystone_getFrustum();
    void keystone_setFrustum(Frustum frustum);
    
    BufferBuilderStorage keystone_getBufferBuilders();
    void keystone_setBufferBuilders(BufferBuilderStorage bufferBuilders);
    
    void keystone_setChunkBuilder(ChunkBuilder chunkBuilder);
}
