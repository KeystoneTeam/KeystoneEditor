package keystone.core.mixins;

import keystone.core.events.minecraft.LivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "baseTick", at = @At("HEAD"), cancellable = true)
    public void beforeBaseTick(CallbackInfo callback)
    {
        if (!LivingEntityEvents.UPDATE.invoker().shouldTick((LivingEntity)((Object)this))) callback.cancel();
    }
}
