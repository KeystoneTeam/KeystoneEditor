package keystone.core.utils;

import keystone.api.Keystone;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
            NbtCompound nbt = NbtIo.readCompressed(inputStream, NbtSizeTracker.ofUnlimitedBytes());
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
    public static List<RegistryEntry<net.minecraft.world.biome.Biome>> deserializeBiomePalette(NbtList paletteNBT)
    {
        RegistryWrapper<net.minecraft.world.biome.Biome> biomeRegistry = RegistryLookups.registryLookup(RegistryKeys.BIOME);
        List<RegistryEntry<net.minecraft.world.biome.Biome>> palette = new ArrayList<>();

        for (int i = 0; i < paletteNBT.size(); i++)
        {
            RegistryKey<net.minecraft.world.biome.Biome> biomeKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(paletteNBT.getString(i)));
            Optional<RegistryEntry.Reference<net.minecraft.world.biome.Biome>> biome = biomeRegistry.getOptional(biomeKey);
            
            if (biome.isPresent()) palette.add(biome.get());
            else
            {
                Keystone.LOGGER.error("Trying to deserialize unregistered biome '{}'!", paletteNBT.getString(i));
                return null;
            }
        }
        return palette;
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
