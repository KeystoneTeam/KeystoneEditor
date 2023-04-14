package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.modules.session.SessionModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin
{
    @Shadow @Final private LevelSummary level;
    
    @Inject(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget$WorldEntry;openReadingWorldScreen()V"))
    private void start_repairSession(CallbackInfo callback)
    {
        // Update Current Save Directory
        Path worldPath = MinecraftClient.getInstance().getLevelStorage().getSavesDirectory().resolve(level.getName());
        KeystoneDirectories.setCurrentSaveDirectory(worldPath);
        
        // Repair Session if Necessary
        Keystone.getModule(SessionModule.class).repairSession();
    }
}
