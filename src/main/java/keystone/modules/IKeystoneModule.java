package keystone.modules;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;

public interface IKeystoneModule
{
    IBoundingBoxProvider[] getBoundingBoxProviders();
    void tick();
}
