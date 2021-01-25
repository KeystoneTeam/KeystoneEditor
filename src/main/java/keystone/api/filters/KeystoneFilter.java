package keystone.api.filters;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class KeystoneFilter
{
    private final IForgeRegistry<Block> blockRegistry;

    public KeystoneFilter()
    {
        blockRegistry = GameRegistry.findRegistry(Block.class);
    }

    public boolean ignoreRepeatBlocks() { return true; }
    public void processBox(FilterBox box) {}
    public void processBlock(int x, int y, int z, FilterBox box)  {}

    //region API
    protected keystone.api.block.Block block(String id)
    {
        Block block = blockRegistry.getValue(new ResourceLocation(id));
        return new keystone.api.block.Block(block.getDefaultState());
    }
    //endregion
}
