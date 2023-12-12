package keystone.core.mixins;

import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
    @Inject(method = "tickBlock", at = @At("HEAD"), cancellable = true)
    private void tickBlock(BlockPos pos, Block block, CallbackInfo callback)
    {
        // TODO: Find a way to reimplement this
        //if (Keystone.isEnabled() && KeystoneGlobalState.SuppressingBlockTicks) callback.cancel();
    }
}
