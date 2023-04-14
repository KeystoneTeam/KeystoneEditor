package keystone.core.mixins.client;

import keystone.api.KeystoneDirectories;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin
{
    @Shadow String saveDirectoryName;
    
    @Inject(method = "startServer", at = @At("HEAD"))
    private void setCurrentWorldPath(CallbackInfo callback)
    {
        // Update Current Save Directory
        Path worldPath = MinecraftClient.getInstance().getLevelStorage().getSavesDirectory().resolve(saveDirectoryName);
        KeystoneDirectories.setCurrentSaveDirectory(worldPath);
    }
}
