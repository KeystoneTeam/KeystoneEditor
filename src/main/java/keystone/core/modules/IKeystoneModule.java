package keystone.core.modules;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

public interface IKeystoneModule
{
    boolean isEnabled();

    default void postInit() {  }
    default void resetModule() {  }
    default IBoundingBoxProvider[] getBoundingBoxProviders() { return null; }
    default void preRender(MatrixStack stack, float partialTicks, DimensionId dimensionId) {  }
}
