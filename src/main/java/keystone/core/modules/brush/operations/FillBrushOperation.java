package keystone.core.modules.brush.operations;

import keystone.api.enums.BlockRetrievalMode;
import keystone.api.variables.Variable;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.brush.BrushOperation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FillBrushOperation extends BrushOperation
{
    @Variable BlockMask mask = new BlockMask().with("minecraft:air");
    @Variable BlockPalette palette = new BlockPalette().with("minecraft:stone");
    @Variable boolean useMask = false;

    @Override
    public ITextComponent getName()
    {
        return new TranslationTextComponent("keystone.brush.fill");
    }
    @Override
    public boolean process(int x, int y, int z, BlocksModule blocks, int iteration)
    {
        Block existing = blocks.getBlock(x, y, z, BlockRetrievalMode.LAST_SWAPPED);
        if (!useMask || mask.valid(existing))
        {
            blocks.setBlock(x, y, z, palette.randomBlock());
            return true;
        }
        else return false;
    }
}