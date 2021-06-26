package keystone.core.schematic.formats;

import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.Block;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.utils.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.util.Constants;

import java.io.File;

public class KeystoneSchematicFormat implements ISchematicFormat
{
    private static final String[] EXTENSIONS = new String[] { "nbt", "kschem" };

    @Override
    public String[] getFileExtensions()
    {
        return EXTENSIONS;
    }

    @Override
    public KeystoneSchematic loadFile(File file)
    {
        CompoundNBT nbt = NBTSerializer.deserialize(file);
        if (nbt.isEmpty()) return null;

        ListNBT sizeNBT = nbt.getList("size", Constants.NBT.TAG_INT);
        Vector3i size = new Vector3i(sizeNBT.getInt(0), sizeNBT.getInt(1), sizeNBT.getInt(2));

        Block[] palette = loadPalette(nbt.getList("palette", Constants.NBT.TAG_COMPOUND));
        ListNBT blocksNBT = nbt.getList("blocks", Constants.NBT.TAG_COMPOUND);

        Block[] blocks = new Block[size.getX() * size.getY() * size.getZ()];
        loadBlocks(size, blocks, blocksNBT, palette);

        return new KeystoneSchematic(size, blocks);
    }

    private Block[] loadPalette(ListNBT paletteNBT)
    {
        Block[] palette = new Block[paletteNBT.size()];
        for (int i = 0; i < palette.length; i++)
        {
            CompoundNBT entry = paletteNBT.getCompound(i);
            String blockString = entry.getString("Name");
            if (entry.contains("Properties"))
            {
                CompoundNBT properties = entry.getCompound("Properties");
                blockString += "[";
                int j = properties.getAllKeys().size();
                for (String key : properties.getAllKeys())
                {
                    blockString += key + "=" + properties.getString(key);
                    j--;
                    if (j > 0) blockString += ",";
                }
                blockString += "]";
            }
            palette[i] = KeystoneFilter.block(blockString);
        }
        return palette;
    }
    private void loadBlocks(Vector3i size, Block[] blocks, ListNBT blocksNBT, Block[] palette)
    {
        for (int i = 0; i < blocksNBT.size(); i++)
        {
            CompoundNBT blockNBT = blocksNBT.getCompound(i);
            Block block = palette[blockNBT.getInt("state")].clone();
            if (blockNBT.contains("nbt")) block.setTileEntity(blockNBT.getCompound("nbt"));

            ListNBT posNBT = blockNBT.getList("pos", Constants.NBT.TAG_INT);
            blocks[index(size, new int[] { posNBT.getInt(0), posNBT.getInt(1), posNBT.getInt(2) })] = block;
        }
    }

    private int index(Vector3i size, int[] pos)
    {
        return pos[2] + pos[1] * size.getZ() + pos[0] * size.getZ() * size.getY();
    }
}
