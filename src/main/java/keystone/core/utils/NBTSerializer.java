package keystone.core.utils;

import keystone.api.Keystone;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class NBTSerializer
{
    //region Files
    public static boolean serialize(String path, NbtCompound nbt)
    {
        return serialize(new File(path), nbt);
    }
    public static boolean serialize(File file, NbtCompound nbt)
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
                NbtIo.writeCompressed(nbt, outputStream);
                return true;
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static NbtCompound deserialize(String path)
    {
        return deserialize(new File(path));
    }
    public static NbtCompound deserialize(File file)
    {
        Path path = file.toPath();
        if (!Files.exists(path)) return new NbtCompound();

        try (InputStream inputStream = new FileInputStream(file))
        {
            NbtCompound nbt = NbtIo.readCompressed(inputStream);
            if (nbt == null) nbt = new NbtCompound();
            return nbt;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return new NbtCompound();
        }
    }
    //endregion
    //region Short Arrays
    public static NbtList serializeShortArray(short[] shorts)
    {
        NbtList listNBT = new NbtList();
        for (short s : shorts) listNBT.add(NbtShort.of(s));
        return listNBT;
    }
    public static short[] deserializeShortArray(NbtList listNBT)
    {
        short[] shorts = new short[listNBT.size()];
        for (int i = 0; i < shorts.length; i++) shorts[i] = listNBT.getShort(i);
        return shorts;
    }
    //endregion
    //region BlockType Arrays
    public static NbtList serializeBlockTypes(BlockType[] blockTypes)
    {
        short[] indices = new short[blockTypes.length];
        for (int i = 0; i < blockTypes.length; i++) indices[i] = blockTypes[i] == null ? -1 : blockTypes[i].getKeystoneID();
        return serializeShortArray(indices);
    }
    public static BlockType[] deserializeBlockTypes(NbtList blockTypesNBT)
    {
        BlockType[] blockTypes = new BlockType[blockTypesNBT.size()];
        for (int i = 0; i < blockTypes.length; i++)
        {
            short id = blockTypesNBT.getShort(i);
            blockTypes[i] = id < 0 ? null : BlockTypeRegistry.fromKeystoneID(blockTypesNBT.getShort(i));
        }
        return blockTypes;
    }
    //endregion
    //region Block Arrays
    public static NbtList serializeBlockPalette(BlockState[] palette)
    {
        List<BlockState> paletteList = new ArrayList<>();
        Collections.addAll(paletteList, palette);

        NbtList paletteNBT = new NbtList();
        for (int i = 0; i < paletteList.size(); i++)
        {
            BlockState entry = paletteList.get(i);
            NbtCompound entryNBT = NbtHelper.fromBlockState(entry);
            paletteNBT.add(i, entryNBT);
        }
        return paletteNBT;
    }
    public static BlockType[] deserializeBlockPalette(NbtList paletteNBT)
    {
        BlockType[] palette = new BlockType[paletteNBT.size()];
        for (int i = 0; i < palette.length; i++)
        {
            NbtCompound entry = paletteNBT.getCompound(i);
            BlockState blockState = NbtHelper.toBlockState(entry);
            palette[i] = BlockTypeRegistry.fromMinecraftBlock(blockState);
        }
        return palette;
    }

    public static NbtCompound serializeBlocks(Block[] blocks)
    {
        NbtCompound nbt = new NbtCompound();

        // Palette
        List<BlockState> palette = generatePalette(blocks);
        NbtList paletteNBT = new NbtList();
        for (int i = 0; i < palette.size(); i++)
        {
            BlockState entry = palette.get(i);
            NbtCompound entryNBT = NbtHelper.fromBlockState(entry);
            paletteNBT.add(i, entryNBT);
        }
        nbt.put("palette", paletteNBT);

        // Blocks
        {
            NbtList blocksNBT = new NbtList();
            for (Block block : blocks)
            {
                NbtCompound blockNBT = new NbtCompound();
                if (block == null) blockNBT.putInt("state", -1);
                else
                {
                    blockNBT.putInt("state", palette.indexOf(block.blockType().getMinecraftBlock()));

                    NBTCompound tileEntityNBT = block.tileEntity();
                    if (tileEntityNBT != null && !tileEntityNBT.getMinecraftNBT().isEmpty())
                    {
                        tileEntityNBT.remove("x");
                        tileEntityNBT.remove("y");
                        tileEntityNBT.remove("z");
                        blockNBT.put("nbt", tileEntityNBT.getMinecraftNBT());
                    }
                }
                blocksNBT.add(blockNBT);
            }
            nbt.put("blocks", blocksNBT);
        }

        return nbt;
    }
    public static Block[] deserializeBlocks(NbtCompound blocksNBT)
    {
        BlockType[] palette = deserializeBlockPalette(blocksNBT.getList("palette", NbtElement.COMPOUND_TYPE));
        NbtList statesNBT = blocksNBT.getList("blocks", NbtElement.COMPOUND_TYPE);
        Block[] blocks = new Block[statesNBT.size()];
        loadBlocks(blocks, statesNBT, palette);
        return blocks;
    }

    private static List<BlockState> generatePalette(Block[] blockTypes)
    {
        List<BlockState> palette = new ArrayList<>();
        for (Block block : blockTypes)
        {
            if (block == null) continue;
            BlockState paletteEntry = block.blockType().getMinecraftBlock();
            if (!palette.contains(paletteEntry)) palette.add(paletteEntry);
        }
        palette.sort(Comparator.comparing(BlockState::toString));
        return palette;
    }
    private static void loadBlocks(Block[] blocks, NbtList blocksNBT, BlockType[] palette)
    {
        for (int i = 0; i < blocksNBT.size(); i++)
        {
            NbtCompound blockNBT = blocksNBT.getCompound(i);
            int state = blockNBT.getInt("state");
            if (state < 0) blocks[i] = null;
            else
            {
                BlockType blockType = palette[state];
                if (blockNBT.contains("nbt"))
                {
                    NBTCompound tileEntity = new NBTCompound(blockNBT.getCompound("nbt"));
                    blocks[i] = new Block(blockType, tileEntity);
                }
                else blocks[i] = new Block(blockType);
            }
        }
    }
    //endregion
    //region Biome Arrays
    public static NbtCompound serializeBiomes(Biome[] biomes)
    {
        NbtCompound nbt = new NbtCompound();

        // Palette
        List<Biome> palette = generatePalette(biomes);
        NbtList paletteNBT = new NbtList();
        for (int i = 0; i < palette.size(); i++)
        {
            Biome entry = palette.get(i);
            if (entry != null) paletteNBT.add(i, NbtString.of(entry.id()));
        }
        nbt.put("palette", paletteNBT);

        // Biomes
        List<Integer> biomesNBT = new ArrayList<>();
        for (Biome biome : biomes) biomesNBT.add(biome == null ? -1 : palette.indexOf(biome));
        nbt.putIntArray("biomes", biomesNBT);

        return nbt;
    }
    public static List<RegistryEntry<net.minecraft.world.biome.Biome>> deserializeBiomePalette(NbtList paletteNBT)
    {
        Registry<net.minecraft.world.biome.Biome> biomeRegistry = WorldRegistries.getBiomeRegistry();
        List<RegistryEntry<net.minecraft.world.biome.Biome>> palette = new ArrayList<>();

        for (int i = 0; i < paletteNBT.size(); i++)
        {
            Optional<net.minecraft.world.biome.Biome> biome = biomeRegistry.getOrEmpty(new Identifier(paletteNBT.getString(i)));
            if (biome.isEmpty())
            {
                Keystone.LOGGER.error("Trying to deserialize unregistered biome '" + paletteNBT.getString(i) + "'!");
                return null;
            }
            else
            {
                Optional<RegistryKey<net.minecraft.world.biome.Biome>> biomeKey = biomeRegistry.getKey(biome.get());
                if (biomeKey.isPresent()) palette.add(biomeRegistry.getEntry(biomeKey.get()).get());
                else
                {
                    Keystone.LOGGER.error("Trying to deserialize unregistered biome entry '" + paletteNBT.getString(i) + "'!");
                    return null;
                }
            }
        }
        return palette;
    }
    public static Biome[] deserializeBiomes(NbtCompound nbt)
    {
        List<RegistryEntry<net.minecraft.world.biome.Biome>> rawPalette = deserializeBiomePalette(nbt.getList("palette", NbtElement.STRING_TYPE));
        List<Biome> palette = new ArrayList<>(rawPalette.size());
        for (RegistryEntry<net.minecraft.world.biome.Biome> biome : rawPalette) palette.add(new Biome(biome));

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
    public static NbtCompound serializeEntities(Map<UUID, Entity> entities)
    {
        NbtCompound nbt = new NbtCompound();
        for (Map.Entry<UUID, Entity> entry : entities.entrySet()) nbt.put(entry.getKey().toString(), entry.getValue().serialize());
        return nbt;
    }
    public static Map<UUID, Entity> deserializeEntities(NbtCompound nbt)
    {
        Map<UUID, Entity> entities = new HashMap<>();
        for (String key : nbt.getKeys())
        {
            Entity entity = Entity.deserialize(nbt.getCompound(key));
            entities.put(UUID.fromString(key), entity);
        }
        return entities;
    }
    //endregion
    //region Tile Entity Maps
    public static NbtList serializeTileEntities(Map<BlockPos, NBTCompound> tileEntities)
    {
        NbtList listNBT = new NbtList();
        for (Map.Entry<BlockPos, NBTCompound> entry : tileEntities.entrySet())
        {
            NbtCompound entityNBT = new NbtCompound();
            entityNBT.putIntArray("pos", new int[] { entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ() });
            entityNBT.put("nbt", entry.getValue().getMinecraftNBT());
            listNBT.add(entityNBT);
        }
        return listNBT;
    }
    public static Map<BlockPos, NBTCompound> deserializeTileEntities(NbtList tileEntitiesNBT)
    {
        Map<BlockPos, NBTCompound> ret = new HashMap<>();
        for (int i = 0; i < tileEntitiesNBT.size(); i++)
        {
            NbtCompound entityNBT = tileEntitiesNBT.getCompound(i);
            int[] pos = entityNBT.getIntArray("pos");
            ret.put(new BlockPos(pos[0], pos[1], pos[2]), new NBTCompound(entityNBT.getCompound("nbt")));
        }
        return ret;
    }
    //endregion
}
