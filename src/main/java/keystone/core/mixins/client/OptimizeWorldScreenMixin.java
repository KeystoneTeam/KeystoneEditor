package keystone.core.mixins.client;

import keystone.core.KeystoneGlobalState;
import net.minecraft.client.gui.screen.world.OptimizeWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OptimizeWorldScreen.class)
public class OptimizeWorldScreenMixin
{
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/updater/WorldUpdater;<init>(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/registry/DynamicRegistryManager;ZZ)V"), index = 4)
    private boolean recreateRegionFilesWhenPurging(boolean original)
    {
        return original || KeystoneGlobalState.PurgeUnvisitedChunks;
    }
}
