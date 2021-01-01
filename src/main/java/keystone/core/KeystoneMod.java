package keystone.core;

import keystone.api.Keystone;
import keystone.core.events.KeystoneEvent;
import keystone.modules.history.HistoryModule;
import keystone.modules.selection.SelectionModule;
import keystone.modules.selection.boxes.HighlightBoundingBox;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.renderers.HighlightBoxRenderer;
import keystone.modules.selection.renderers.SelectionBoxRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KeystoneMod.MODID)
public class KeystoneMod
{
    public static final String MODID = "keystone";

    public KeystoneMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        Keystone.LOGGER.info("Triggering Keystone initialization events");

        Keystone.init();
        registerDefaultBoxes(new KeystoneEvent.RegisterBoundingBoxTypes());
        registerDefaultModules(new KeystoneEvent.RegisterModules());

//        TODO: Figure out why events aren't working
//        MinecraftForge.EVENT_BUS.post(new KeystoneEvent.RegisterBoundingBoxTypes());
//        MinecraftForge.EVENT_BUS.post(new KeystoneEvent.RegisterModules());
    }
    private void clientSetup(final FMLClientSetupEvent event)
    {
        KeystoneKeybinds.register();
    }

    public void registerDefaultBoxes(final KeystoneEvent.RegisterBoundingBoxTypes event)
    {
        event.register(SelectionBoundingBox.class, new SelectionBoxRenderer(), "selection_box");
        event.register(HighlightBoundingBox.class, new HighlightBoxRenderer(), "highlight_box");
    }
    private void registerDefaultModules(final KeystoneEvent.RegisterModules event)
    {
        event.register(new HistoryModule());
        event.register(new SelectionModule());
    }
}
