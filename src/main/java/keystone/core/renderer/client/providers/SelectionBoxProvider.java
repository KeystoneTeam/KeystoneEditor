package keystone.core.renderer.client.providers;

import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.BoundingBoxCuboid;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;

import java.util.HashSet;
import java.util.Set;

public class SelectionBoxProvider implements IBoundingBoxProvider<BoundingBoxCuboid>
{
    private static Set<BoundingBoxCuboid> test = new HashSet<>();
    static
    {
        test.add(BoundingBoxCuboid.from(new Coords(-1, 0, -1), new Coords(1, 0, 1), BoundingBoxType.SelectionBox));
    }

    @Override
    public Iterable<BoundingBoxCuboid> get(DimensionId dimensionId)
    {
        return test;
    }
}
