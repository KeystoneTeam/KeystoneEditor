import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.BlockMask;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.api.wrappers.nbt.NBTList;
import keystone.api.wrappers.nbt.NBTType;

public class EmptyInventories extends KeystoneFilter
{
    @Variable BlockMask mask = whitelist();
    
    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        if (mask.valid(region.getBlockType(x, y, z)))
        {
            NBTCompound blockData = region.getBlockData(x, y, z);
            if (blockData.contains("Items", NBTType.LIST))
            {
                print("Found Inventory at " + x + ", " + y + ", " + z);
                blockData.put("Items", new NBTList());
                region.setBlockData(x, y, z, blockData);
            }
            else print("No Inventory at " + x + ", " + y + ", " + z);
        }
    }
}