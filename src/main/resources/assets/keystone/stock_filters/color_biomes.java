import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.BlockType;

import java.util.ArrayList;
import java.util.List;

public class ColorBiomes extends KeystoneFilter
{
    @Tooltip("Whether to smooth the biomes the same way Minecraft does.")
    @Variable boolean smooth = false;
    
    private BlockType[] biomeColors = new BlockType[]
    {
            blockType("minecraft:white_stained_glass"),
            blockType("minecraft:orange_stained_glass"),
            blockType("minecraft:magenta_stained_glass"),
            blockType("minecraft:light_blue_stained_glass"),
            blockType("minecraft:yellow_stained_glass"),
            blockType("minecraft:lime_stained_glass"),
            blockType("minecraft:pink_stained_glass"),
            blockType("minecraft:gray_stained_glass"),
            blockType("minecraft:light_gray_stained_glass"),
            blockType("minecraft:cyan_stained_glass"),
            blockType("minecraft:purple_stained_glass"),
            blockType("minecraft:blue_stained_glass"),
            blockType("minecraft:brown_stained_glass"),
            blockType("minecraft:green_stained_glass"),
            blockType("minecraft:red_stained_glass"),
            blockType("minecraft:black_stained_glass")
    };
    private List biomeColorIndices = new ArrayList();

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        Biome biome = region.getBiome(x, y, z, smooth);
        int index = biomeColorIndices.indexOf(biome);
        if (index < 0)
        {
            index = biomeColorIndices.size();
            biomeColorIndices.add(biome);
            if (index >= biomeColors.length)
            {
                cancel("More than " + biomeColors.length + " biomes found!");
                return;
            }
        }
        region.setBlock(x, y, z, biomeColors[index]);
    }
}