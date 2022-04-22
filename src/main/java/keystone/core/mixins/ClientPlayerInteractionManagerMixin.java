package keystone.core.mixins;

import keystone.core.events.minecraft.ClientPlayerEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin
{
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void allowInteractBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> callback)
    {
        if (!ClientPlayerEvents.ALLOW_USE_BLOCK.invoker().allow(player, world, hand, hitResult)) callback.setReturnValue(ActionResult.PASS);
    }
}
