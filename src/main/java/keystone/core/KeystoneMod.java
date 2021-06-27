package keystone.core;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.events.KeystoneEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.keybinds.KeystoneKeybinds;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.brush.BrushModule;
import keystone.core.modules.brush.boxes.BrushPositionBox;
import keystone.core.modules.brush.boxes.BrushPreviewBox;
import keystone.core.modules.brush.renderers.BrushPositionBoxRenderer;
import keystone.core.modules.brush.renderers.BrushPreviewBoxRenderer;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.filter.FilterModule;
import keystone.core.modules.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.modules.schematic_import.renderers.ImportBoxRenderer;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.HighlightBoundingBox;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.selection.renderers.HighlightBoxRenderer;
import keystone.core.modules.selection.renderers.SelectionBoxRenderer;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KeystoneMod.MODID)
public class KeystoneMod
{
    public static final String MODID = "keystone";
    private static boolean initialized = false;

    public KeystoneMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.addListener(this::gameLoaded);
        MinecraftForge.EVENT_BUS.addListener(this::registerDefaultBoxes);
        MinecraftForge.EVENT_BUS.addListener(this::registerDefaultModules);
        MinecraftForge.EVENT_BUS.addListener(this::registerDefaultSchematicFormats);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        KeystoneDirectories.init();
    }
    private void clientSetup(final FMLClientSetupEvent event)
    {
        KeystoneOverlayHandler.addOverlay(new KeystoneHotbar());
        KeystoneKeybinds.register();
    }

    private void gameLoaded(final GuiOpenEvent event)
    {
        if (initialized) return;
        else initialized = true;

        Keystone.LOGGER.info("Triggering Keystone initialization events");

        Keystone.init();
        MinecraftForge.EVENT_BUS.post(new KeystoneEvent.RegisterBoundingBoxTypes());
        MinecraftForge.EVENT_BUS.post(new KeystoneEvent.RegisterModules());
        MinecraftForge.EVENT_BUS.post(new KeystoneEvent.RegisterSchematicFormats());
        Keystone.postInit();
    }
    private void registerDefaultBoxes(final KeystoneEvent.RegisterBoundingBoxTypes event)
    {
        Keystone.LOGGER.info("Registering default Keystone bounding box types");

        event.register(SelectionBoundingBox.class, new SelectionBoxRenderer(), "selection_box");
        event.register(HighlightBoundingBox.class, new HighlightBoxRenderer(), "highlight_box");
        event.register(ImportBoundingBox.class, new ImportBoxRenderer(), "import_box");
        event.register(BrushPositionBox.class, new BrushPositionBoxRenderer(), "brush_position");
        event.register(BrushPreviewBox.class, new BrushPreviewBoxRenderer(), "brush_preview");
    }
    private void registerDefaultModules(final KeystoneEvent.RegisterModules event)
    {
        Keystone.LOGGER.info("Registering default Keystone modules");

        event.register(new MouseModule());
        event.register(new WorldCacheModule());
        event.register(new BlocksModule());
        event.register(new GhostBlocksModule());
        event.register(new HistoryModule());
        event.register(new ClipboardModule());

        event.register(new SelectionModule());
        event.register(new BrushModule());
        event.register(new ImportModule());
        event.register(new FilterModule());
    }
    private void registerDefaultSchematicFormats(final KeystoneEvent.RegisterSchematicFormats event)
    {
        event.register(new KeystoneSchematicFormat());
    }
}
