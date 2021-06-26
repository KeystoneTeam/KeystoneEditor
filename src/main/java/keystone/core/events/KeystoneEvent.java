package keystone.core.events;

import keystone.api.Keystone;
import keystone.core.renderer.client.ClientRenderer;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.modules.IKeystoneModule;
import keystone.core.schematic.SchematicLoader;
import keystone.core.schematic.formats.ISchematicFormat;
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

    public static class RegisterSchematicFormats extends KeystoneEvent
    {
        public void register(ISchematicFormat format)
        {
            SchematicLoader.registerFormat(format);
        }
        public void registerAll(ISchematicFormat... formats)
        {
            for (ISchematicFormat format : formats) register(format);
        }
    }
}
