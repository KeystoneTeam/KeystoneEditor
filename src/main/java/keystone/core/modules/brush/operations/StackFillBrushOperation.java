package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public class StackFillBrushOperation extends BrushOperation
{
    @Variable BlockPalette palette = new BlockPalette().with("minecraft:stone");
    @Variable boolean stack = true;
    @Variable boolean gravity = true;

    @Override
    public Text getName()
    {
        return Text.translatable("keystone.brush.stackFill");
    }
    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        int newY = y;
        BlockType current = worldModifiers.blocks.getBlockType(x, newY, z, RetrievalMode.CURRENT);
        if (current.isAir())
        {
            if (gravity)
            {
                while (current.isAir())
                {
                    newY--;
                    current = worldModifiers.blocks.getBlockType(x, newY, z, RetrievalMode.CURRENT);
                }
                newY++;
            }
            else if (worldModifiers.blocks.getBlockType(x, newY - 1, z, RetrievalMode.CURRENT).isAir()) return true;
        }
        else
        {
            if (stack)
            {
                while (!current.isAir())
                {
                    newY++;
                    current = worldModifiers.blocks.getBlockType(x, newY, z, RetrievalMode.CURRENT);
                }
            }
            else return true;
        }

        worldModifiers.blocks.setBlock(x, newY, z, palette.randomBlock());
        return true;
    }
}