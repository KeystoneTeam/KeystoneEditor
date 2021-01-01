package keystone.modules;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

public interface IKeystoneModule
{
    default IBoundingBoxProvider[] getBoundingBoxProviders() { return null; }
    default void prepareRender(float partialTicks, DimensionId dimensionId) {  }
}
