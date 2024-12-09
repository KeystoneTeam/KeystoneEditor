package keystone.core.modules.brush.operations;

import keystone.api.BlockMask;
import keystone.api.BlockPalette;
import keystone.api.enums.RetrievalMode;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;

public class FillBrushOperation extends BrushOperation
{
    @Tooltip("Only blocks matching this mask will be replaced.")
    @Variable BlockMask mask = new BlockMask().blacklist();
    
    @Tooltip("The palette to use when filling the brush shape.")
    @Variable BlockPalette palette = new BlockPalette().with("minecraft:stone");

    @Override
    public Text getName()
    {
        return Text.translatable("keystone.brush.fill");
    }
    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        BlockState existing = worldModifiers.blocks.getBlockState(x, y, z, RetrievalMode.LAST_SWAPPED);
        if (mask.valid(existing))
        {
            worldModifiers.blocks.setBlockType(x, y, z, palette.randomBlockState());
            return true;
        }
        else return false;
    }
}