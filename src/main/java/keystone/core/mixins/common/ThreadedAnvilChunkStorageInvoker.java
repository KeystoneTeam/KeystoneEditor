package keystone.core.mixins.common;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkLoadingManager.class)
public interface ThreadedAnvilChunkStorageInvoker
{
    @Invoker("entryIterator")
    Iterable<ChunkHolder> getEntryIterator();
}
