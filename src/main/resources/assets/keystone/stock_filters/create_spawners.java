import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;

public class CreateSpawners extends KeystoneFilter
{
    @Variable boolean keepPosition = false;

    public boolean allowBlocksOutsideRegion() { return true; }

    public void processEntity(Entity entity, WorldRegion region)
    {
        NBTCompound entityData = entity.data();
        if (!keepPosition)
        {
            entityData.remove("Pos");
            entityData.remove("Rotation");
        }

        NBTCompound tileEntity = new NBTCompound();
        NBTCompound spawnData = new NBTCompound();
        spawnData.put("entity", entityData);
        tileEntity.put("SpawnData", spawnData);

        BlockPos blockPos = entity.blockPos();
        region.setBlock(blockPos.x, blockPos.y, blockPos.z, block("minecraft:spawner", tileEntity));
        entity.kill();
    }
}