package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickFallFlying()V"))
    private void applyCustomDeceleration(CallbackInfo callback)
    {
        if (Keystone.isEnabled() && (Object)this == MinecraftClient.getInstance().player)
        {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;
            Input input = player.input;

            player.forwardSpeed /= 0.98;
            player.sidewaysSpeed /= 0.98;

            if (input.jumping || input.sneaking || player.forwardSpeed != 0 || player.sidewaysSpeed != 0 || !player.getAbilities().flying) return;
            player.setVelocity(player.getVelocity().multiply(KeystoneConfig.flySmoothing));
        }
    }
}
