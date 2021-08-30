import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.api.wrappers.nbt.NBTList;

public class CreateSpawners extends KeystoneFilter
{
    @Variable boolean keepPosition = false;

    public boolean allowBlocksOutsideRegion() { return true; }

    public void processEntity(Entity entity, WorldRegion region)
    {
        NBTCompound entityData = entity.data();
        entityData.remove("UUID");
        if (!keepPosition)
        {
            entityData.remove("Pos");
            entityData.remove("Rotation");
        }

        NBTCompound entitySpawn = new NBTCompound();
        entitySpawn.put("Entity", entity.data());
        entitySpawn.putInt("Weight", 1);

        NBTList spawnPotentials = new NBTList();
        spawnPotentials.add(entitySpawn);

        Block spawner = block("minecraft:spawner");
        NBTCompound spawnerData = spawner.tileEntity();
        spawnerData.put("SpawnPotentials", spawnPotentials);

        region.setBlock((int)Math.floor(entity.boundingBox().centerX), (int)Math.floor(entity.y()), (int)Math.floor(entity.boundingBox().centerZ), spawner);
        entity.kill();
    }
}