package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import net.minecraft.text.Text;

public class StackFillBrushOperation extends BrushOperation
{
    @Tooltip("The blocks that represent empty space.")
    @Variable BlockMask airMask = new BlockMask().with("minecraft:air").whitelist();
    
    @Tooltip("The palette to use when filling the brush shape.")
    @Variable BlockPalette palette = new BlockPalette().with("minecraft:stone");
    
    @Tooltip("If checked, blocks that should be placed inside non-empty space will instead be placed in the first empty space above them.")
    @Variable boolean stack = true;
    
    @Tooltip("If checked, blocks that should be placed in empty space will instead be placed above the first non-empty space below them.")
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
        if (airMask.valid(current))
        {
            if (gravity)
            {
                while (airMask.valid(current))
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
                while (!airMask.valid(current))
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