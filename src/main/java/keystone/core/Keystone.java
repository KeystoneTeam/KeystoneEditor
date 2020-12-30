package keystone.core;

import keystone.core.events.KeystoneEvent;
import keystone.modules.IKeystoneModule;
import keystone.modules.selection.SelectionBox;
import keystone.modules.selection.SelectionBoxRenderer;
import keystone.modules.selection.SelectionModule;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();

    //region Module Registry
    private static List<IKeystoneModule> modules = new ArrayList<>();

    public static void registerModule(IKeystoneModule module)
    {
        modules.add(module);
    }
    public static void unregisterModule(IKeystoneModule module)
    {
        modules.remove(module);
    }
    public static void forEachModule(Consumer<IKeystoneModule> consumer)
    {
        modules.forEach(consumer);
    }
    //endregion
    //region Events
    @SubscribeEvent
    public static void registerDefaultBoxes(final KeystoneEvent.RegisterBoundingBoxTypes event)
    {
        LOGGER.info("Registering Default Box Types");

        event.register(SelectionBox.class, new SelectionBoxRenderer(), "selection_box");
    }

    @SubscribeEvent
    public static void registerDefaultModules(final KeystoneEvent.RegisterModules event)
    {
        LOGGER.info("Registering Default Modules...");

        event.register(new SelectionModule());
    }

    @SubscribeEvent
    public static void tick(final TickEvent.ClientTickEvent event)
    {
        modules.forEach((module) -> module.tick());
    }
    //endregion
}
