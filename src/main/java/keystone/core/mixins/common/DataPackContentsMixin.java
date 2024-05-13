package keystone.core.mixins.common;

import keystone.core.utils.RegistryLookups;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin
{
    @Shadow @Final private DataPackContents.ConfigurableWrapperLookup registryLookup;
    
    @Inject(method = "<init>", at = @At("RETURN"))
    public void postConstructorRegister(DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, CallbackInfo ci)
    {
        RegistryLookups.configureLookups(dynamicRegistryManager, registryLookup, enabledFeatures);
    }
}
