package keystone.core;

import keystone.api.Keystone;
import keystone.api.KeystoneCache;
import keystone.api.KeystoneDirectories;
import keystone.core.events.KeystoneInputHandler;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.events.keystone.KeystoneRegistryEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.brush.BrushSelectionScreen;
import keystone.core.gui.overlays.fill.FillAndReplaceScreen;
import keystone.core.gui.overlays.filters.FilterSelectionScreen;
import keystone.core.gui.overlays.schematics.CloneScreen;
import keystone.core.gui.overlays.schematics.ImportScreen;
import keystone.core.gui.overlays.selection.SelectionNudgeScreen;
import keystone.core.gui.overlays.selection.SelectionScreen;
import keystone.core.keybinds.KeystoneKeyBindings;
import keystone.core.modules.brush.BrushModule;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.filter.FilterModule;
import keystone.core.modules.filter.providers.BlockListProvider;
import keystone.core.modules.filter.providers.BlockTypeProvider;
import keystone.core.modules.filter.remapper.FilterRemapper;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.CloneScreenHistoryEntry;
import keystone.core.modules.history.entries.ImportBoxesHistoryEntry;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.hotkeys.HotkeysModule;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.rendering.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.rendering.world_highlight.WorldHighlightModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.session.SessionModule;
import keystone.core.modules.world.BlocksModule;
import keystone.core.modules.world.EntitiesModule;
import keystone.core.modules.world.biomes.BiomesModule;
import keystone.core.modules.world.change_queue.WorldChangeQueueModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.schematic.extensions.BiomesExtension;
import keystone.core.schematic.extensions.StructureVoidsExtension;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import keystone.core.serialization.VariablesSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.IOException;

public class KeystoneMod implements ModInitializer, ClientModInitializer
{
    public static final String MODID = "keystone";
    private static boolean ranGameLoaded = false;
    private static boolean ranVersionCheck = false;

    @Override
    public void onInitialize()
    {
        try
        {
            KeystoneDirectories.init();
            KeystoneCache.init();
            DebugFlags.init();
            FilterRemapper.init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        KeystoneLifecycleEvents.GAME_LOADED.register(this::gameLoaded);

        KeystoneRegistryEvents.MODULES.register(registry ->
        {
            Keystone.LOGGER.info("Registering default Keystone modules");

            registry.accept(new MouseModule());
            registry.accept(new HistoryModule());
            registry.accept(new ClipboardModule());
            registry.accept(new SessionModule());
            registry.accept(new HotkeysModule());
            
            registry.accept(new WorldCacheModule());
            registry.accept(new BlocksModule());
            registry.accept(new BiomesModule());
            registry.accept(new EntitiesModule());
            registry.accept(new WorldChangeQueueModule());

            registry.accept(new GhostBlocksModule());
            registry.accept(new WorldHighlightModule());

            registry.accept(new SelectionModule());
            registry.accept(new BrushModule());
            registry.accept(new ImportModule());
            registry.accept(new FilterModule());
        });

        KeystoneRegistryEvents.HISTORY_ENTRIES.register(new KeystoneRegistryEvents.RegisterHistoryEntriesListener()
        {
            @Override
            public void onRegister()
            {
                Keystone.LOGGER.info("Registering default Keystone history entries");

                register("import_boxes", ImportBoxesHistoryEntry::new);
                register("selection_boxes", SelectionHistoryEntry::new);
                register("clone_screen", CloneScreenHistoryEntry::new);
            }
        });

        KeystoneRegistryEvents.SCHEMATIC_FORMATS.register(registry ->
        {
            Keystone.LOGGER.info("Registering default Keystone schematic formats");

            registry.accept(new KeystoneSchematicFormat());
        });

        KeystoneRegistryEvents.SCHEMATIC_EXTENSIONS.register(registry ->
        {
            Keystone.LOGGER.info("Registering default Keystone schematic extensions");

            registry.accept(new BiomesExtension());
            registry.accept(new StructureVoidsExtension());
        });
        
        KeystoneRegistryEvents.VARIABLE_SERIALIZERS.register(VariablesSerializer::registerDefaultSerializers);
        
        KeystoneRegistryEvents.BLOCK_PROVIDER_TYPES.register(new KeystoneRegistryEvents.RegisterBlockProviderTypesListener()
        {
            @Override
            public void onRegister()
            {
                register(new Identifier("keystone:block_type"), BlockTypeProvider.class);
                register(new Identifier("keystone:block_list"), BlockListProvider.class);
            }
        });
    }
    @Override
    public void onInitializeClient()
    {
        KeystoneLifecycleEvents.OPEN_WORLD.register(this::onOpenWorld);
        KeystoneLifecycleEvents.CLOSE_WORLD.register(this::onCloseWorld);

        KeystoneInputHandler.registerEvents();
        BrushSelectionScreen.registerEvents();
        FillAndReplaceScreen.registerEvents();
        FilterSelectionScreen.registerEvents();
        CloneScreen.registerEvents();
        ImportScreen.registerEvents();
        SelectionNudgeScreen.registerEvents();
        SelectionScreen.registerEvents();
        KeystoneOverlayHandler.registerEvents();

        KeystoneKeyBindings.register();
    }
    
    public static void tryGameLoaded()
    {
        if (!ranGameLoaded)
        {
            KeystoneLifecycleEvents.GAME_LOADED.invoker().onLoaded();
            ranGameLoaded = true;
        }
    }

    private void gameLoaded()
    {
        Keystone.LOGGER.info("Triggering Keystone initialization events");
        Keystone.init();

        // Register Modules
        KeystoneRegistryEvents.registerModules();

        // Register Serializers and Load Config
        KeystoneRegistryEvents.registerVariableSerializers();
        KeystoneConfig.load();

        // Register Rest of Content
        KeystoneRegistryEvents.registerHistoryEntries();
        KeystoneRegistryEvents.registerSchematicFormats();
        KeystoneRegistryEvents.registerSchematicExtensions();
        KeystoneRegistryEvents.registerBlockProviderTypes();

        Keystone.postInit();
    }

    private void onOpenWorld(World world)
    {
        if (KeystoneConfig.startActive) Keystone.enableKeystone();
        else Keystone.disableKeystone();
    
        if (!ranVersionCheck)
        {
            ranVersionCheck = true;
            VersionChecker.doVersionCheck();
        }
    
        KeystoneKeyBindings.configureKeyConditions();
    }
    private void onCloseWorld()
    {
        Keystone.disableKeystone();
        Keystone.forEachModule(module -> module.resetModule());
        KeystoneCache.cleanTempFiles();
    }
}
