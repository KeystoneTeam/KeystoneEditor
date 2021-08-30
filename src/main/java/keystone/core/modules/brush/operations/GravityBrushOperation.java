package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GravityBrushOperation extends BrushOperation
{
    @Override
    public ITextComponent getName()
    {
        return new TranslationTextComponent("keystone.brush.gravity");
    }
    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        BlockType existing = worldModifiers.blocks.getBlockType(x, y, z, RetrievalMode.CURRENT);
        if (!existing.isAir())
        {
            int newY = y - 1;
            BlockType current = worldModifiers.blocks.getBlockType(x, newY, z, RetrievalMode.CURRENT);
            if (!current.isAir()) return true;
            else
            {
                while (current.isAir())
                {
                    newY --;
                    current = worldModifiers.blocks.getBlockType(x, newY, z, RetrievalMode.CURRENT);
                }
                newY++;
            }

            worldModifiers.blocks.setBlock(x, newY, z, existing);
            worldModifiers.blocks.setBlock(x, y, z, BlockTypeRegistry.AIR);
        }

        return true;
    }
}
