package keystone.core.events;

import keystone.api.Keystone;
import keystone.core.renderer.client.ClientRenderer;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.modules.IKeystoneModule;
import net.minecraftforge.eventbus.api.Event;

public class KeystoneEvent extends Event
{
    public static class RegisterModules extends KeystoneEvent
    {
        public void register(IKeystoneModule module)
        {
            Keystone.registerModule(module);
            IBoundingBoxProvider[] providers = module.getBoundingBoxProviders();
            if (providers != null) for (IBoundingBoxProvider provider : providers) ClientRenderer.registerProvider(provider);
        }
        public void registerAll(IKeystoneModule... modules)
        {
            for (IKeystoneModule module : modules) register(module);
        }
    }

    public static class RegisterBoundingBoxTypes extends KeystoneEvent
    {
        public <T extends AbstractBoundingBox> void register(Class<T> boxClass, AbstractRenderer<T> renderer, String name)
        {
            BoundingBoxType.register(name);
            ClientRenderer.registerRenderer(boxClass, renderer);
        }
    }
}
