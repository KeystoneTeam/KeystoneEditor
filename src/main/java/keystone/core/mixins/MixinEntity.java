package keystone.core.mixins;

import keystone.core.Keystone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity
{
    @Shadow
    public boolean noClip;

    @Shadow
    public EntityType<?> getType()
    {
        throw new IllegalStateException("Mixin failed to shadow getType()");
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void tick(CallbackInfo callback)
    {
        if (getType() == EntityType.PLAYER && Keystone.Active) noClip = true;
    }
}
