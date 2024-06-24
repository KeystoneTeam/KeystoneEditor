package keystone.core.mixins.common;

import com.llamalad7.mixinextras.sugar.Local;
import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/world/updater/WorldUpdater$RegionUpdate")
public class RegionUpdateMixin
{
    @ModifyArg(method = "update(Lnet/minecraft/world/storage/VersionedChunkStorage;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/registry/RegistryKey;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/VersionedChunkStorage;setNbt(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/NbtCompound;)Ljava/util/concurrent/CompletableFuture;"), index = 1)
    private NbtCompound purgeUnvisitedChunks(NbtCompound chunkNBT, @Local(argsOnly = true) ChunkPos chunkPos)
    {
        if (KeystoneGlobalState.PurgeUnvisitedChunks)
        {
            long inhabitedTime = chunkNBT.contains("InhabitedTime", NbtElement.LONG_TYPE) ? chunkNBT.getLong("InhabitedTime") : 0;
            if (inhabitedTime <= KeystoneGlobalState.UnvisitedChunkCutoff)
            {
                Keystone.LOGGER.info("Purged {}. Time: {}", chunkPos, inhabitedTime);
                return null;
            }
        }
        
        Keystone.LOGGER.info("Kept {}", chunkPos);
        return chunkNBT;
    }
}
