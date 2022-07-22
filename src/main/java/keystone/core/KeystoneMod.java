package keystone.core;

import keystone.api.Keystone;
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
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.CloneScreenHistoryEntry;
import keystone.core.modules.history.entries.ImportBoxesHistoryEntry;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.rendering.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.rendering.world_highlight.WorldHighlightModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.session.SessionModule;
import keystone.core.modules.world.BlocksModule;
import keystone.core.modules.world.EntitiesModule;
import keystone.core.modules.world.WorldChangeQueueModule;
import keystone.core.modules.world.biomes.BiomesModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.schematic.extensions.BiomesExtension;
import keystone.core.schematic.extensions.StructureVoidsExtension;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;

public class KeystoneMod implements ModInitializer, ClientModInitializer
{
    public static final String MODID = "keystone";
    private static boolean initialized = false;
    private static boolean ranVersionCheck = false;
    private static boolean inWorld;

    @Override
    public void onInitialize()
    {
        try
        {
            KeystoneDirectories.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        KeystoneRegistryEvents.MODULES.register(registry ->
        {
            Keystone.LOGGER.info("Registering default Keystone modules");

            registry.accept(new MouseModule());
            registry.accept(new WorldCacheModule());
            registry.accept(new BlocksModule());
            registry.accept(new BiomesModule());
            registry.accept(new EntitiesModule());
            registry.accept(new HistoryModule());
            registry.accept(new ClipboardModule());
            registry.accept(new WorldChangeQueueModule());
            registry.accept(new SessionModule());

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
    }
    @Override
    public void onInitializeClient()
    {
        ScreenEvents.AFTER_INIT.register(this::gameLoaded);
        ScreenEvents.AFTER_INIT.register(this::onWorldLeft);
        ClientEntityEvents.ENTITY_LOAD.register(this::onWorldLoaded);

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

    private void gameLoaded(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight)
    {
        if (initialized) return;
        else initialized = true;

        Keystone.LOGGER.info("Triggering Keystone initialization events");
        Keystone.init();

        KeystoneRegistryEvents.registerModules();
        KeystoneRegistryEvents.registerHistoryEntries();
        KeystoneRegistryEvents.registerSchematicFormats();
        KeystoneRegistryEvents.registerSchematicExtensions();

        Keystone.postInit();
    }

    private void onWorldLoaded(Entity entity, ClientWorld world)
    {
        if (entity instanceof PlayerEntity && !inWorld)
        {
            if (KeystoneConfig.startActive) Keystone.enableKeystone();
            else Keystone.disableKeystone();
            inWorld = true;

            if (!ranVersionCheck)
            {
                ranVersionCheck = true;
                VersionChecker.doVersionCheck();
            }

            KeystoneKeyBindings.configureKeyConditions();
            KeystoneLifecycleEvents.OPEN_WORLD.invoker().join(world);
        }
    }
    private void onWorldLeft(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight)
    {
        if (screen instanceof TitleScreen && inWorld)
        {
            inWorld = false;

            Keystone.disableKeystone();
            Keystone.forEachModule(module -> module.resetModule());
            KeystoneLifecycleEvents.CLOSE_WORLD.invoker().leave();
        }
    }
}
