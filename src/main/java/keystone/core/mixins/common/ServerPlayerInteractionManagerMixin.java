package keystone.core.mixins.common;

import keystone.api.Keystone;
import keystone.core.events.minecraft.ServerPlayerEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin
{
    @Inject(method = "changeGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;setGameMode(Lnet/minecraft/world/GameMode;Lnet/minecraft/world/GameMode;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void changeGameMode(GameMode gameMode, CallbackInfoReturnable<Boolean> callback)
    {
        if (Keystone.isEnabled() && gameMode != GameMode.SPECTATOR)
        {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void allowInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> callback)
    {
        if (!ServerPlayerEvents.ALLOW_USE_BLOCK.invoker().allow(player, world, stack, hand, hitResult)) callback.setReturnValue(ActionResult.PASS);
    }
}
