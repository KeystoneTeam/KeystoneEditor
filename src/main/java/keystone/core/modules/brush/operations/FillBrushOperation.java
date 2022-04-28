package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class FillBrushOperation extends BrushOperation
{
    @Variable BlockMask mask = new BlockMask().blacklist();
    @Variable BlockPalette palette = new BlockPalette().with("minecraft:stone");

    @Override
    public Text getName()
    {
        return new TranslatableText("keystone.brush.fill");
    }
    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        BlockType existing = worldModifiers.blocks.getBlockType(x, y, z, RetrievalMode.LAST_SWAPPED);
        if (mask.valid(existing))
        {
            worldModifiers.blocks.setBlock(x, y, z, palette.randomBlock());
            return true;
        }
        else return false;
    }
}