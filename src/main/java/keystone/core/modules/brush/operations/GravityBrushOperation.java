package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;

public class GravityBrushOperation extends BrushOperation
{
    @Override
    public Text getName()
    {
        return Text.translatable("keystone.brush.gravity");
    }
    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        BlockState existing = worldModifiers.blocks.getBlockState(x, y, z, RetrievalMode.CURRENT);
        if (!existing.isAir())
        {
            int newY = y - 1;
            BlockState current = worldModifiers.blocks.getBlockState(x, newY, z, RetrievalMode.CURRENT);
            if (!current.isAir()) return true;
            else
            {
                while (current.isAir())
                {
                    newY --;
                    current = worldModifiers.blocks.getBlockState(x, newY, z, RetrievalMode.CURRENT);
                }
                newY++;
            }

            worldModifiers.blocks.setBlockType(x, newY, z, existing);
            worldModifiers.blocks.setBlockType(x, y, z, Blocks.AIR.getDefaultState());
        }

        return true;
    }
}
