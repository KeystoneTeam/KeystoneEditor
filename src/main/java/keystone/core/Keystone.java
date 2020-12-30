package keystone.core;

import keystone.core.events.KeystoneEvent;
import keystone.core.renderer.config.KeystoneConfig;
import keystone.modules.IKeystoneModule;
import keystone.modules.selection.boxes.HighlightBoundingBox;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.renderers.HighlightBoxRenderer;
import keystone.modules.selection.renderers.SelectionBoxRenderer;
import keystone.modules.selection.SelectionModule;
import net.minecraftforge.client.event.DrawHighlightEvent;
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

    //region Active Toggle
    public static boolean Active = KeystoneConfig.startActive;

    public static void toggleKeystone()
    {
        if (Active) Active = false;
        else Active = true;
    }
    //endregion
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
    //region Keystone Events
    @SubscribeEvent
    public static void registerDefaultBoxes(final KeystoneEvent.RegisterBoundingBoxTypes event)
    {
        LOGGER.info("Registering Default Box Types");

        event.register(SelectionBoundingBox.class, new SelectionBoxRenderer(), "selection_box");
        event.register(HighlightBoundingBox.class, new HighlightBoxRenderer(), "highlight_box");
    }

    @SubscribeEvent
    public static void registerDefaultModules(final KeystoneEvent.RegisterModules event)
    {
        LOGGER.info("Registering Default Modules...");

        event.register(new SelectionModule());
    }
    //endregion
    //region Forge Events
    @SubscribeEvent
    public static void tick(final TickEvent.ClientTickEvent event)
    {
        modules.forEach((module) -> module.tick());
    }

    @SubscribeEvent
    public static void drawHighlight(final DrawHighlightEvent event)
    {
        if (Active) event.setCanceled(true);
    }
    //endregion
}
