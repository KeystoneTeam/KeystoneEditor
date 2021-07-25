package keystone.api.tools;

import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.entities.Entity;

public class DeleteEntitiesTool extends KeystoneFilter
{
    @Override
    public void processEntity(Entity entity, WorldRegion region)
    {
        entity.kill();
    }
}
