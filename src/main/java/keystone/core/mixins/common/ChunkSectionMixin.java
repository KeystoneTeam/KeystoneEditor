package keystone.core.mixins.common;

import keystone.core.mixins.interfaces.KeystoneChunkSection;
import keystone.core.utils.PalettedContainerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.*;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements KeystoneChunkSection
{
    @Shadow @Mutable @Final private PalettedContainer<BlockState> blockStateContainer;
    
    @Shadow private short nonEmptyBlockCount;
    
    @Shadow private short randomTickableBlockCount;
    
    @Shadow private short nonEmptyFluidCount;
    
    @Unique
    public void keystone_copyFrom(ChunkSection other)
    {
        ChunkSectionMixin otherMixin = (ChunkSectionMixin) (Object) other;
        
        blockStateContainer = PalettedContainerUtils.copyContainer(other.getBlockStateContainer());
        nonEmptyBlockCount = otherMixin.nonEmptyBlockCount;
        randomTickableBlockCount = otherMixin.randomTickableBlockCount;
        nonEmptyFluidCount = otherMixin.nonEmptyFluidCount;
    }
}
