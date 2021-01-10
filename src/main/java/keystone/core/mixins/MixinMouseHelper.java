package keystone.core.mixins;

import keystone.api.Keystone;
import net.minecraft.client.MouseHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(MouseHelper.class)
public class MixinMouseHelper
{
    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    public void grabMouse(CallbackInfo callback)
    {
        if (Keystone.isActive() && !Keystone.AllowPlayerLook) callback.cancel();
    }
}
