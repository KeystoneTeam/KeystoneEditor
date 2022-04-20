package keystone.core.modules;

import keystone.core.renderer.shapes.SelectableBoundingBox;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.Collection;
import java.util.Collections;

public interface IKeystoneModule
{
    boolean isEnabled();

    default void postInit() {  }
    default void resetModule() {  }
    default void preRender(WorldRenderContext context) {  }
    default void alwaysRender(WorldRenderContext context) {  }
    default void renderWhenEnabled(WorldRenderContext context) {  }
    default Collection<? extends SelectableBoundingBox> getSelectableBoxes() { return Collections.emptyList(); }
}
