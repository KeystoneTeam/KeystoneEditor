package keystone.core.schematic.extensions;

import keystone.api.wrappers.entities.Entity;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.utils.PalettedArray;
import keystone.core.utils.RegistryLookups;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Map;

public class BiomesExtension implements ISchematicExtension
{
    private PalettedArray<RegistryEntry<Biome>> biomes;

    @Override
    public BiomesExtension create(World world, BlockBox bounds)
    {
        PalettedArray<RegistryEntry<Biome>> biomes = new PalettedArray<>(bounds.getBlockCountX() * bounds.getBlockCountY() * bounds.getBlockCountZ());
        Vec3i extents = bounds.getDimensions().add(1, 1, 1);
        bounds.forEachVertex(pos -> biomes.set(getIndex(pos, extents), world.getBiome(pos)));
        
        BiomesExtension extension = new BiomesExtension();
        extension.biomes = biomes;
        return extension;
    }
    private int getIndex(BlockPos pos, Vec3i size)
    {
        return pos.getZ() + pos.getY() * size.getZ() + pos.getX() * size.getZ() * size.getY();
    }
    private BlockPos getPos(int index, Vec3i size)
    {
        int x = index / (size.getZ() * size.getY());
        int y = (index - x * size.getZ() * size.getY()) / size.getZ();
        int z = index - x * size.getZ() * size.getY() - y * size.getZ();
        return new BlockPos(x, y, z);
    }

    @Override
    public Identifier id()
    {
        return Identifier.of("keystone:biomes");
    }

    @Override
    public NbtCompound serialize(KeystoneSchematic schematic)
    {
        return biomes.serialize(paletteEntry ->
        {
            Identifier id = paletteEntry.getKey().orElseThrow().getValue();
            return NbtString.of(id.toString());
        });
    }

    @Override
    public ISchematicExtension deserialize(Vec3i size, PalettedArray<BlockState> blocks, Map<BlockPos, NbtCompound> tileEntities, Entity[] entities, NbtCompound nbt)
    {
        BiomesExtension extension = new BiomesExtension();
        extension.biomes = new PalettedArray<>(nbt, paletteNbt ->
        {
            assert paletteNbt instanceof NbtString;
            Identifier biomeID = Identifier.of(paletteNbt.asString());
            RegistryKey<Biome> biomeKey = RegistryKey.of(RegistryKeys.BIOME, biomeID);
            return RegistryLookups.registryLookup(RegistryKeys.BIOME).getOrThrow(biomeKey);
        });
        return extension;
    }

    @Override
    public boolean canPlace() { return true; }
}
