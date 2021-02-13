package keystone.core;

import keystone.api.Keystone;
import keystone.core.events.KeystoneEvent;
import keystone.core.keybinds.KeystoneKeybinds;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.modules.brush.BrushModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.clipboard.boxes.PasteBoundingBox;
import keystone.core.modules.clipboard.renderers.PasteBoxRenderer;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.HighlightBoundingBox;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.selection.renderers.HighlightBoxRenderer;
import keystone.core.modules.selection.renderers.SelectionBoxRenderer;
import keystone.core.modules.world_cache.WorldCacheModule;
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
        KeystoneOverlayHandler.addOverlay(new KeystoneHotbar());
        KeystoneKeybinds.register();
    }

    private void registerDefaultBoxes(final KeystoneEvent.RegisterBoundingBoxTypes event)
    {
        event.register(SelectionBoundingBox.class, new SelectionBoxRenderer(), "selection_box");
        event.register(HighlightBoundingBox.class, new HighlightBoxRenderer(), "highlight_box");
        event.register(PasteBoundingBox.class, new PasteBoxRenderer(), "paste_box");
    }
    private void registerDefaultModules(final KeystoneEvent.RegisterModules event)
    {
        event.register(new MouseModule());
        event.register(new WorldCacheModule());
        event.register(new HistoryModule());
        event.register(new SelectionModule());
        event.register(new BrushModule());
        event.register(new ClipboardModule());
    }
}
