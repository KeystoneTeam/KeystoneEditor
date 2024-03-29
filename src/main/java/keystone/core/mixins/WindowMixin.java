package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Window.class)
public class WindowMixin
{
    @Shadow private int framebufferHeight;

    @ModifyVariable(method = "setScaleFactor", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double smartScaleFactor(double initialValue)
    {
        if (Keystone.isEnabled()) return Math.max(KeystoneConfig.minGuiScale, Math.round(this.framebufferHeight / 720.0));
        else return initialValue;
    }
}
