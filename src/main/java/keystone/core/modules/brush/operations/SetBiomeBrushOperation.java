package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.utils.WorldRegistries;
import net.minecraft.text.Text;
import net.minecraft.world.biome.BiomeKeys;

public class SetBiomeBrushOperation extends BrushOperation
{
    @Tooltip("Only blocks matching this mask will have their biome changed.")
    @Variable BlockMask mask = new BlockMask().blacklist();

    @Tooltip("The biome to change the brush shape to.")
    @Variable Biome biome = new Biome(WorldRegistries.getBiomeRegistry().getEntry(BiomeKeys.PLAINS).get());

    @Override
    public Text getName()
    {
        return Text.translatable("keystone.brush.setBiome");
    }

    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        BlockType blockType = worldModifiers.blocks.getBlockType(x, y, z, RetrievalMode.LAST_SWAPPED);
        if (mask.valid(blockType))
        {
            worldModifiers.biomes.setBiome(x, y, z, biome);
            return true;
        }
        return false;
    }
}
