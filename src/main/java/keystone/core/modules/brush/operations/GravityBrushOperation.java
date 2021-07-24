package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.brush.BrushOperation;
import net.minecraft.block.Blocks;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GravityBrushOperation extends BrushOperation
{
    private final Block air = new Block(Blocks.AIR.defaultBlockState());

    @Override
    public ITextComponent getName()
    {
        return new TranslationTextComponent("keystone.brush.gravity");
    }
    @Override
    public boolean process(int x, int y, int z, BlocksModule blocks, int iteration)
    {
        Block existing = blocks.getBlock(x, y, z, RetrievalMode.CURRENT);
        if (!existing.isAir())
        {
            int newY = y - 1;
            Block current = blocks.getBlock(x, newY, z, RetrievalMode.CURRENT);
            if (!current.isAir()) return true;
            else
            {
                while (current.isAir())
                {
                    newY --;
                    current = blocks.getBlock(x, newY, z, RetrievalMode.CURRENT);
                }
                newY++;
            }

            blocks.setBlock(x, newY, z, existing);
            blocks.setBlock(x, y, z, air);
        }

        return true;
    }
}
