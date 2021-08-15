package keystone.core.mixins;

import com.mojang.datafixers.util.Function4;
import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.KeystoneConfig;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Shadow @Final private MainWindow window;

    @Inject(method = "pauseGame", at = @At(value = "HEAD"), cancellable = true)
    public void pauseGame(boolean pauseOnly, CallbackInfo callback)
    {
        if (Keystone.isActive() && KeystoneConfig.disableInGameMenu) callback.cancel();
    }

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    public void resizeDisplay(CallbackInfo callback)
    {
        KeystoneOverlayHandler.resize(Minecraft.getInstance(), this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
    }

    @Inject(method = "loadWorld", at = @At("HEAD"), remap = false)
    public void loadWorld(String p_238195_1_, DynamicRegistries.Impl p_238195_2_, Function<SaveFormat.LevelSave, DatapackCodec> p_238195_3_, Function4<SaveFormat.LevelSave, DynamicRegistries.Impl, IResourceManager, DatapackCodec, IServerConfiguration> p_238195_4_, boolean p_238195_5_, Minecraft.WorldSelectionType p_238195_6_, boolean creating, CallbackInfo ci)
    {
        KeystoneDirectories.setCurrentLevelID(p_238195_1_);
    }
}
