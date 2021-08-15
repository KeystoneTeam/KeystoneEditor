package keystone.core.utils;

import keystone.api.Keystone;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.entities.Entity;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class NBTSerializer
{
    //region Files
    public static boolean serialize(String path, CompoundNBT nbt)
    {
        return serialize(new File(path), nbt);
    }
    public static boolean serialize(File file, CompoundNBT nbt)
    {
        Path path = file.toPath();
        Path parent = path.getParent();

        if (parent == null) return false;
        else
        {
            // Create parent directories if necessary
            try
            {
                Files.createDirectories(Files.exists(parent) ? parent.toRealPath() : parent);
            }
            catch (IOException ioexception)
            {
                Keystone.LOGGER.error("Failed to create parent directory: {}", parent);
                return false;
            }

            // Serialize nbt
            try (OutputStream outputStream = new FileOutputStream(file))
            {
                CompressedStreamTools.writeCompressed(nbt, outputStream);
                return true;
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static CompoundNBT deserialize(String path)
    {
        return deserialize(new File(path));
    }
    public static CompoundNBT deserialize(File file)
    {
        Path path = file.toPath();
        if (!Files.exists(path)) return new CompoundNBT();

        try (InputStream inputStream = new FileInputStream(file))
        {
            CompoundNBT nbt = CompressedStreamTools.readCompressed(inputStream);
            if (nbt == null) nbt = new CompoundNBT();
            return nbt;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return new CompoundNBT();
        }
    }
    //endregion
    //region Block Arrays
    public static CompoundNBT serializeBlocks(Block[] blocks)
    {
        CompoundNBT nbt = new CompoundNBT();

        // Palette
        List<BlockState> palette = generatePalette(blocks);
        ListNBT paletteNBT = new ListNBT();
        for (int i = 0; i < palette.size(); i++)
        {
            BlockState entry = palette.get(i);
            CompoundNBT entryNBT = NBTUtil.writeBlockState(entry);
            paletteNBT.add(i, entryNBT);
        }
        nbt.put("palette", paletteNBT);

        // Blocks
        {
            ListNBT blocksNBT = new ListNBT();
            for (Block block : blocks)
            {
                CompoundNBT blockNBT = new CompoundNBT();
                if (block == null) blockNBT.putInt("state", -1);
                else
                {
                    blockNBT.putInt("state", palette.indexOf(block.getMinecraftBlock()));

                    CompoundNBT tileEntityNBT = block.getTileEntityData();
                    if (tileEntityNBT != null && !tileEntityNBT.isEmpty())
                    {
                        tileEntityNBT.remove("x");
                        tileEntityNBT.remove("y");
                        tileEntityNBT.remove("z");
                        blockNBT.put("nbt", tileEntityNBT);
                    }
                }
                blocksNBT.add(blockNBT);
            }
            nbt.put("blocks", blocksNBT);
        }

        return nbt;
    }
    public static Block[] deserializeBlocks(CompoundNBT blocksNBT)
    {
        Block[] palette = loadPalette(blocksNBT.getList("palette", Constants.NBT.TAG_COMPOUND));
        ListNBT statesNBT = blocksNBT.getList("blocks", Constants.NBT.TAG_COMPOUND);
        Block[] blocks = new Block[statesNBT.size()];
        loadBlocks(blocks, statesNBT, palette);
        return blocks;
    }

    private static List<BlockState> generatePalette(Block[] blocks)
    {
        List<BlockState> palette = new ArrayList<>();
        for (Block block : blocks)
        {
            if (block == null) continue;
            BlockState paletteEntry = block.getMinecraftBlock();
            if (!palette.contains(paletteEntry)) palette.add(paletteEntry);
        }
        palette.sort(Comparator.comparing(BlockState::toString));
        return palette;
    }
    private static Block[] loadPalette(ListNBT paletteNBT)
    {
        Block[] palette = new Block[paletteNBT.size()];
        for (int i = 0; i < palette.length; i++)
        {
            CompoundNBT entry = paletteNBT.getCompound(i);
            BlockState blockState = NBTUtil.readBlockState(entry);
            palette[i] = new Block(blockState);
        }
        return palette;
    }
    private static void loadBlocks(Block[] blocks, ListNBT blocksNBT, Block[] palette)
    {
        for (int i = 0; i < blocksNBT.size(); i++)
        {
            CompoundNBT blockNBT = blocksNBT.getCompound(i);
            int state = blockNBT.getInt("state");
            if (state < 0) blocks[i] = null;
            else
            {
                Block block = palette[state].clone();
                if (blockNBT.contains("nbt")) block.setTileEntity(blockNBT.getCompound("nbt"));
                blocks[i] = block;
            }
        }
    }
    //endregion
    //region Biome Arrays
    public static CompoundNBT serializeBiomes(Biome[] biomes)
    {
        CompoundNBT nbt = new CompoundNBT();

        // Palette
        List<Biome> palette = generatePalette(biomes);
        ListNBT paletteNBT = new ListNBT();
        for (int i = 0; i < palette.size(); i++)
        {
            Biome entry = palette.get(i);
            if (entry != null) paletteNBT.add(i, StringNBT.valueOf(entry.id()));
        }
        nbt.put("palette", paletteNBT);

        // Biomes
        List<Integer> biomesNBT = new ArrayList<>();
        for (Biome biome : biomes) biomesNBT.add(biome == null ? -1 : palette.indexOf(biome));
        nbt.putIntArray("biomes", biomesNBT);

        return nbt;
    }
    public static Biome[] deserializeBiomes(CompoundNBT nbt)
    {
        ListNBT paletteNBT = nbt.getList("palette", Constants.NBT.TAG_STRING);
        List<Biome> palette = new ArrayList<>();
        for (int i = 0; i < paletteNBT.size(); i++) palette.add(new Biome(ForgeRegistries.BIOMES.getValue(new ResourceLocation(paletteNBT.getString(i)))));

        int[] biomeIndices = nbt.getIntArray("biomes");
        Biome[] biomes = new Biome[biomeIndices.length];
        for (int i = 0; i < biomes.length; i++)
        {
            int index = biomeIndices[i];
            if (index < 0) biomes[i] = null;
            else biomes[i] = palette.get(index);
        }
        return biomes;
    }

    private static List<Biome> generatePalette(Biome[] biomes)
    {
        List<Biome> palette = new ArrayList<>();
        for (Biome biome : biomes) if (biome != null && !palette.contains(biome)) palette.add(biome);
        palette.sort(Comparator.comparing(Biome::id));
        return palette;
    }
    //endregion
    //region Entity Maps
    public static CompoundNBT serializeEntities(Map<UUID, Entity> entities)
    {
        CompoundNBT nbt = new CompoundNBT();
        for (Map.Entry<UUID, Entity> entry : entities.entrySet()) nbt.put(entry.getKey().toString(), entry.getValue().serialize());
        return nbt;
    }
    public static Map<UUID, Entity> deserializeEntities(CompoundNBT nbt)
    {
        Map<UUID, Entity> entities = new HashMap<>();
        for (String key : nbt.getAllKeys())
        {
            Entity entity = Entity.deserialize(nbt.getCompound(key));
            entities.put(UUID.fromString(key), entity);
        }
        return entities;
    }
    //endregion
}
