package keystone.core.renderer.client.providers;

import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.DimensionId;

public interface IBoundingBoxProvider<T extends AbstractBoundingBox>
{
    Iterable<T> get(DimensionId dimensionId);
    default boolean canProvide(DimensionId dimensionId) { return true; }
}
