package keystone.core.mixins;

import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(PersistentStateManager.class)
public interface PersistentStateManagerAccessor
{
    @Accessor("directory")
    File getDirectory();
}
