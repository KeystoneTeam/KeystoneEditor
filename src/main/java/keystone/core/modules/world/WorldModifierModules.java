package keystone.core.modules.world;

import keystone.api.Keystone;

public class WorldModifierModules
{
    public final BlocksModule blocks;
    public final BiomesModule biomes;
    public final EntitiesModule entities;

    public WorldModifierModules()
    {
        this(Keystone.getModule(BlocksModule.class), Keystone.getModule(BiomesModule.class), Keystone.getModule(EntitiesModule.class));
    }
    public WorldModifierModules(BlocksModule blocks, BiomesModule biomes, EntitiesModule entities)
    {
        this.blocks = blocks;
        this.biomes = biomes;
        this.entities = entities;
    }
}
