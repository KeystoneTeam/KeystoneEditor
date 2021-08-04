package keystone.core.schematic.extensions;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world.BlocksModule;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class BiomesExtension implements ISchematicExtension
{
    private Biome[] biomes;

    @Override
    public BiomesExtension create(World world, BoundingBox bounds)
    {
        BlocksModule blocks = Keystone.getModule(BlocksModule.class);

        List<Biome> biomesList = new ArrayList<>();
        bounds.forEachCoordinate((x, y, z) ->
        {
            if (blocks.getBlock(x, y, z, RetrievalMode.LAST_SWAPPED).getMinecraftBlock().is(Blocks.STRUCTURE_VOID)) biomesList.add(null);
            else biomesList.add(world.getBiome(new BlockPos(x - bounds.minX, y - bounds.minY, z - bounds.minZ)));
        });

        if (biomesList.size() > 0)
        {
            BiomesExtension extension = new BiomesExtension();
            extension.biomes = new Biome[biomesList.size()];
            biomesList.toArray(extension.biomes);
            return extension;
        }
        else return null;
    }

    @Override
    public ResourceLocation id()
    {
        return new ResourceLocation("keystone:biomes");
    }

    @Override
    public void serialize(KeystoneSchematic schematic, CompoundNBT nbt)
    {
        List<Biome> palette = new ArrayList<>();
        for (Biome biome : biomes) if (biome != null && !palette.contains(biome)) palette.add(biome);
        palette.sort((a, b) -> a.getRegistryName().compareNamespaced(b.getRegistryName()));
        ListNBT paletteNBT = new ListNBT();
        for (Biome biome : palette) paletteNBT.add(StringNBT.valueOf(biome.getRegistryName().toString()));
        nbt.put("palette", paletteNBT);

        ListNBT biomesNBT = new ListNBT();
        int i = 0;
        for (int x = 0; x < schematic.getSize().getX(); x++)
        {
            for (int y = 0; y < schematic.getSize().getY(); y++)
            {
                for (int z = 0; z < schematic.getSize().getZ(); z++)
                {
                    Biome biome = biomes[i];
                    i++;
                    if (biome != null)
                    {
                        ListNBT posNBT = new ListNBT();
                        posNBT.add(IntNBT.valueOf(x));
                        posNBT.add(IntNBT.valueOf(y));
                        posNBT.add(IntNBT.valueOf(z));

                        CompoundNBT biomeNBT = new CompoundNBT();
                        biomeNBT.put("pos", posNBT);
                        biomeNBT.put("biome", IntNBT.valueOf(palette.indexOf(biome)));
                        biomesNBT.add(biomeNBT);
                    }
                }
            }
        }
        nbt.put("biomes", biomesNBT);
    }

    @Override
    public ISchematicExtension deserialize(Vector3i size, Block[] blocks, Entity[] entities, CompoundNBT nbt)
    {
        // Load Biome Palette
        List<Biome> palette = new ArrayList<>();
        ListNBT paletteNBT = nbt.getList("palette", Constants.NBT.TAG_STRING);
        for (int i = 0; i < paletteNBT.size(); i++)
        {
            String biomeID = paletteNBT.getString(i);
            Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeID));
            if (biome == null)
            {
                Keystone.LOGGER.warn("Trying to load schematic with unregistered biome '" + biomeID + "'!");
                return null;
            }
            else palette.add(biome);
        }

        // Load Biomes
        ListNBT biomesNBT = nbt.getList("biomes", Constants.NBT.TAG_COMPOUND);
        Biome[] biomes = new Biome[size.getX() * size.getY() * size.getZ()];
        for (int i = 0; i < biomesNBT.size(); i++)
        {
            CompoundNBT biomeNBT = biomesNBT.getCompound(i);
            ListNBT posNBT = biomeNBT.getList("pos", Constants.NBT.TAG_INT);
            int index = posNBT.getInt(2) + posNBT.getInt(1) * size.getZ() + posNBT.getInt(0) * size.getZ() * size.getY();
            biomes[index] = palette.get(biomeNBT.getInt("biome"));
        }

        // Build Extension
        BiomesExtension extension = new BiomesExtension();
        extension.biomes = biomes;
        return extension;
    }
}
