import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;

public class CreateSpawners extends KeystoneFilter
{
    @Tooltip("If checked, the spawner will always spawn entities at the coordinates of the template entity.")
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

        region.setBlockData(entity.blockPos(), tileEntity);
        entity.kill();
    }
}