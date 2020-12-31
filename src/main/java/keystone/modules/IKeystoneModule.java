package keystone.modules;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

public interface IKeystoneModule
{
    IBoundingBoxProvider[] getBoundingBoxProviders();
    default void prepareRender(float partialTicks, DimensionId dimensionId) {  }
}
