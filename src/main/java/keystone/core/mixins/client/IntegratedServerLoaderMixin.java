package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.core.modules.session.SessionModule;
import net.minecraft.server.integrated.IntegratedServerLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin
{
    @ModifyVariable(method = "start(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/serialization/Dynamic;ZZLjava/lang/Runnable;)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private boolean cancelBackupScreen(boolean initialValue)
    {
        if (Keystone.getModule(SessionModule.class).isRevertingSessionChanges()) return false;
        else return initialValue;
    }
}
