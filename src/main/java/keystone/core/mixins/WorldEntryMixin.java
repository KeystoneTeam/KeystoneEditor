package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.modules.session.SessionModule;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldListWidget.WorldEntry.class)
public class WorldEntryMixin
{
    @Shadow @Final private LevelSummary level;

    @Inject(method = "start", at = @At("HEAD"))
    public void startHead(CallbackInfo callback)
    {
        Keystone.getModule(SessionModule.class).setLevel(level);
    }
}
