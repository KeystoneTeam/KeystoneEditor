package keystone.core.modules;

import keystone.api.Keystone;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.entities.EntitiesModule;

public class WorldModifierModules
{
    public final BlocksModule blocks;
    public final EntitiesModule entities;

    public WorldModifierModules() { this(Keystone.getModule(BlocksModule.class), Keystone.getModule(EntitiesModule.class)); }
    public WorldModifierModules(BlocksModule blocks, EntitiesModule entities)
    {
        this.blocks = blocks;
        this.entities = entities;
    }
}
