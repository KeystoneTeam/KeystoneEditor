package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin
{
    @Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    public void canPlaceAtOverride(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        if (Keystone.isEnabled() && KeystoneGlobalState.SuppressPlacementChecks) cir.setReturnValue(true);
    }
}
