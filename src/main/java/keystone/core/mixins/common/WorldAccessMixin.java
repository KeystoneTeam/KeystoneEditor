package keystone.core.mixins.common;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldAccess.class)
public interface WorldAccessMixin
{
    @Inject(method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V", at = @At("HEAD"), cancellable = true)
    private void suppressBlockTicks0(BlockPos pos, Block block, int delay, CallbackInfo callback)
    {
        if (Keystone.isEnabled() && KeystoneGlobalState.BlockTickScheduling) callback.cancel();
    }
    @Inject(method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;ILnet/minecraft/world/tick/TickPriority;)V", at = @At("HEAD"), cancellable = true)
    private void suppressBlockTicks1(BlockPos pos, Block block, int delay, TickPriority priority, CallbackInfo callback)
    {
        if (Keystone.isEnabled() && KeystoneGlobalState.BlockTickScheduling) callback.cancel();
    }
    @Inject(method = "scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;I)V", at = @At("HEAD"), cancellable = true)
    private void suppressFluidTicks0(BlockPos pos, Fluid fluid, int delay, CallbackInfo callback)
    {
        if (Keystone.isEnabled() && KeystoneGlobalState.BlockTickScheduling) callback.cancel();
    }
    @Inject(method = "scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;ILnet/minecraft/world/tick/TickPriority;)V", at = @At("HEAD"), cancellable = true)
    private void suppressFluidTicks1(BlockPos pos, Fluid fluid, int delay, TickPriority priority, CallbackInfo callback)
    {
        if (Keystone.isEnabled() && KeystoneGlobalState.BlockTickScheduling) callback.cancel();
    }
}
