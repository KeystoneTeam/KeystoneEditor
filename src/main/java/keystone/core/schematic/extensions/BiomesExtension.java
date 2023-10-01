package keystone.core.schematic.extensions;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world.BlocksModule;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.utils.WorldRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BiomesExtension implements ISchematicExtension
{
    private RegistryEntry<Biome>[] biomes;

    @Override
    public BiomesExtension create(World world, BoundingBox bounds)
    {
        BlocksModule blocks = Keystone.getModule(BlocksModule.class);

        List<RegistryEntry<Biome>> biomesList = new ArrayList<>();
        bounds.forEachCoordinate((x, y, z) ->
        {
            if (blocks.getBlockType(x, y, z, RetrievalMode.LAST_SWAPPED).getMinecraftBlock().isOf(Blocks.STRUCTURE_VOID)) biomesList.add(null);
            else biomesList.add(world.getBiome(new BlockPos(x - bounds.minX, y - bounds.minY, z - bounds.minZ)));
        });

        if (biomesList.size() > 0)
        {
            BiomesExtension extension = new BiomesExtension();
            extension.biomes = new RegistryEntry[biomesList.size()];
            biomesList.toArray(extension.biomes);
            return extension;
        }
        else return null;
    }

    @Override
    public Identifier id()
    {
        return new Identifier("keystone:biomes");
    }

    @Override
    public void serialize(KeystoneSchematic schematic, NbtCompound nbt)
    {
        List<RegistryEntry<Biome>> palette = new ArrayList<>();
        for (RegistryEntry<Biome> biome : biomes) if (biome != null && !palette.contains(biome)) palette.add(biome);
        palette.sort(Comparator.comparing(a -> a.getKey().get().getValue()));
        NbtList paletteNBT = new NbtList();
        for (RegistryEntry<Biome> biome : palette) paletteNBT.add(NbtString.of(biome.getKey().get().getValue().toString()));
        nbt.put("palette", paletteNBT);

        NbtList biomesNBT = new NbtList();
        int i = 0;
        for (int x = 0; x < schematic.getSize().getX(); x++)
        {
            for (int y = 0; y < schematic.getSize().getY(); y++)
            {
                for (int z = 0; z < schematic.getSize().getZ(); z++)
                {
                    RegistryEntry<Biome> biome = biomes[i];
                    i++;
                    if (biome != null)
                    {
                        NbtList posNBT = new NbtList();
                        posNBT.add(NbtInt.of(x));
                        posNBT.add(NbtInt.of(y));
                        posNBT.add(NbtInt.of(z));

                        NbtCompound biomeNBT = new NbtCompound();
                        biomeNBT.put("pos", posNBT);
                        biomeNBT.put("biome", NbtInt.of(palette.indexOf(biome)));
                        biomesNBT.add(biomeNBT);
                    }
                }
            }
        }
        nbt.put("biomes", biomesNBT);
    }

    @Override
    public ISchematicExtension deserialize(Vec3i size, Block[] blocks, Entity[] entities, NbtCompound nbt)
    {
        // Load Biome Palette
        List<RegistryEntry<Biome>> palette = new ArrayList<>();
        NbtList paletteNBT = nbt.getList("palette", NbtElement.STRING_TYPE);
        Registry<Biome> biomeRegistry = WorldRegistries.getBiomeRegistry();
        for (int i = 0; i < paletteNBT.size(); i++)
        {
            String biomeID = paletteNBT.getString(i);
            Optional<Biome> biome = biomeRegistry.getOrEmpty(new Identifier(biomeID));
            if (biome.isEmpty())
            {
                Keystone.LOGGER.warn("Trying to load schematic with unregistered biome '" + biomeID + "'!");
                return null;
            }
            else
            {
                Optional<RegistryKey<Biome>> biomeKey = biomeRegistry.getKey(biome.get());
                if (biomeKey.isPresent()) palette.add(biomeRegistry.getEntry(biomeKey.get()).get());
                else
                {
                    Keystone.LOGGER.warn("Trying to load schematic with unregistered biome entry '" + biomeID + "'!");
                    return null;
                }
            }
        }

        // Load Biomes
        NbtList biomesNBT = nbt.getList("biomes", NbtElement.COMPOUND_TYPE);
        RegistryEntry<Biome>[] biomes = new RegistryEntry[size.getX() * size.getY() * size.getZ()];
        for (int i = 0; i < biomesNBT.size(); i++)
        {
            NbtCompound biomeNBT = biomesNBT.getCompound(i);
            NbtList posNBT = biomeNBT.getList("pos", NbtElement.INT_TYPE);
            int index = posNBT.getInt(2) + posNBT.getInt(1) * size.getZ() + posNBT.getInt(0) * size.getZ() * size.getY();
            biomes[index] = palette.get(biomeNBT.getInt("biome"));
        }

        // Build Extension
        BiomesExtension extension = new BiomesExtension();
        extension.biomes = biomes;
        return extension;
    }

    @Override
    public boolean canPlace() { return true; }
}
