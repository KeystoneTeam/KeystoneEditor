package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.core.modules.session.SessionModule;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin
{
    @Inject(method = "showBackupPromptScreen", at = @At("HEAD"), cancellable = true)
    private void skipBackupScreen(LevelStorage.Session session, boolean customized, Runnable callback, Runnable onCancel, CallbackInfo ci)
    {
        if (Keystone.getModule(SessionModule.class).isRevertingSessionChanges())
        {
            callback.run();
            ci.cancel();
        }
    }
}
