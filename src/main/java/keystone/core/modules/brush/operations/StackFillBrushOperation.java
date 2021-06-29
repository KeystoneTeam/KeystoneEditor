package keystone.core.modules.brush.operations;

import keystone.api.enums.BlockRetrievalMode;
import keystone.api.variables.Variable;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockPalette;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.brush.BrushOperation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class StackFillBrushOperation extends BrushOperation
{
    @Variable BlockPalette palette = new BlockPalette().with("minecraft:stone");
    @Variable boolean stack = true;
    @Variable boolean gravity = true;

    @Override
    public ITextComponent getName()
    {
        return new TranslationTextComponent("keystone.brush.stackFill");
    }
    @Override
    public boolean process(int x, int y, int z, BlocksModule blocks, int iteration)
    {
        int newY = y;
        Block current = blocks.getBlock(x, newY, z, BlockRetrievalMode.CURRENT);
        if (current.isAir())
        {
            if (gravity)
            {
                while (current.isAir())
                {
                    newY--;
                    current = blocks.getBlock(x, newY, z, BlockRetrievalMode.CURRENT);
                }
                newY++;
            }
            else if (blocks.getBlock(x, newY - 1, z, BlockRetrievalMode.CURRENT).isAir()) return true;
        }
        else
        {
            if (stack)
            {
                while (!current.isAir())
                {
                    newY++;
                    current = blocks.getBlock(x, newY, z, BlockRetrievalMode.CURRENT);
                }
            }
            else return true;
        }

        blocks.setBlock(x, newY, z, palette.randomBlock());
        return true;
    }
}