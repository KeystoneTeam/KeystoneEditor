import keystone.api.KeystoneDirectories;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.IntRange;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.overlays.file_browser.SaveFileScreen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExtractPalette extends KeystoneFilter
{
    @Variable BlockMask includeMask = blacklist();
    @Variable String name = "Palette";
    @Variable @IntRange(min = 1) int resolution = 250;
    
    private Map blockCounts = new HashMap();
    private int count = 0;
    
    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        // Count the block
        BlockType block = region.getBlockType(x, y, z);
        Integer blockCount = (Integer) blockCounts.get(block);
        blockCount = blockCount == null ? 1 : blockCount + 1;
        blockCounts.put(block, blockCount);
        this.count++;
    }
    
    @Override
    public void finished()
    {
        // Build Palette
        BlockPalette palette = palette();
        for (Map.Entry entry : blockCounts.entrySet())
        {
            BlockType block = (BlockType) entry.getKey();
            Integer count = (Integer) entry.getValue();
            int weight = (int)(count / (float)this.count * resolution);
            palette.with(block, weight);
        }
        
        // Save Palette
        File paletteFile = KeystoneDirectories.getPalettesDirectory().resolve(name + ".nbt").toFile();
        palette.write(paletteFile);
    }
}