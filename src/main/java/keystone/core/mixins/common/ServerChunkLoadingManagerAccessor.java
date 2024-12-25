package keystone.core.mixins.common;

import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkLoadingManager.class)
public interface ServerChunkLoadingManagerAccessor
{
    @Accessor("lightingProvider") ServerLightingProvider getLightingProvider();
    
    @Invoker("sendToPlayers") void invokeSendToPlayers(WorldChunk chunk);
}
