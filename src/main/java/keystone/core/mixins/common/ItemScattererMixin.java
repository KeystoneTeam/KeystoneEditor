package keystone.core.mixins.common;

import keystone.api.Keystone;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemScatterer.class)
public class ItemScattererMixin
{
    @Inject(method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void cancelItemSpawn(World world, double x, double y, double z, ItemStack stack, CallbackInfo callback)
    {
        if (Keystone.isEnabled()) callback.cancel();
    }
}
