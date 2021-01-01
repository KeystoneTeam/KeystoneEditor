package keystone.core;

import keystone.api.SelectionBox;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.tools.interfaces.IKeystoneTool;
import keystone.api.tools.interfaces.ISelectionBoxTool;
import keystone.core.events.KeystoneEvent;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.renderer.config.KeystoneConfig;
import keystone.modules.IKeystoneModule;
import keystone.modules.selection.SelectionModule;
import keystone.modules.selection.boxes.HighlightBoundingBox;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.renderers.HighlightBoxRenderer;
import keystone.modules.selection.renderers.SelectionBoxRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static Map<DimensionId, World> loadedWorlds = new HashMap<>();

    //region Active Toggle
    public static boolean Active = KeystoneConfig.startActive;
    public static boolean CloseSelection = false;
    public static boolean RenderHighlightBox = true;

    public static void toggleKeystone()
    {
        if (Active) Active = false;
        else Active = true;
    }
    //endregion
    //region Module Registry
    private static Map<Class, IKeystoneModule> modules = new HashMap<>();

    public static void registerModule(IKeystoneModule module)
    {
        if (modules.containsKey(module.getClass())) LOGGER.error("Trying to register keystone module '" + module.getClass().getSimpleName() + "', when it was already registered!");
        else modules.put(module.getClass(), module);
    }
    public static <T extends IKeystoneModule> T getModule(Class<T> clazz)
    {
        if (modules.containsKey(clazz)) return (T)modules.get(clazz);
        else LOGGER.error("Trying to get unregistered keystone module '" + clazz.getSimpleName() + "'!");

        return null;
    }
    public static void forEachModule(Consumer<IKeystoneModule> consumer)
    {
        modules.values().forEach(consumer);
    }
    //endregion
    //region Tools
    public static void runTool(IKeystoneTool tool)
    {
        DimensionId dimensionId = Player.getDimensionId();
        if (!loadedWorlds.containsKey(dimensionId))
        {
            LOGGER.error("Trying to run keystone tool when there is no loaded world for dimension '" + dimensionId.getDimensionType().getRegistryName() + "'!");
            return;
        }

        World world = loadedWorlds.get(dimensionId);
        SelectionBox[] boxes = getModule(SelectionModule.class).buildSelectionBoxes(world);

        Set<BlockPos> processedBlocks = new HashSet<>();
        for (SelectionBox box : boxes)
        {
            if (tool instanceof ISelectionBoxTool) ((ISelectionBoxTool)tool).process(box);
            if (tool instanceof IBlockTool)
            {
                IBlockTool blockTool = (IBlockTool)tool;
                box.forEachBlock((pos ->
                {
                    if (!blockTool.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                    {
                        blockTool.process(pos, box);
                        processedBlocks.add(pos);
                    }
                }));
            }
        }
        for (SelectionBox box : boxes) box.applyChanges(world);
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
    //region Caching Loaded Worlds
    @SubscribeEvent
    public static void onWorldLoaded(final WorldEvent.Load event)
    {
        if (event.getWorld() instanceof World)
        {
            World world = (World)event.getWorld();
            if (!world.isRemote)
            {
                DimensionId dimensionId = DimensionId.from(world.getDimensionKey());
                if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
                loadedWorlds.put(dimensionId, world);
            }
        }
    }
    @SubscribeEvent
    public static void onWorldUnloaded(final WorldEvent.Unload event)
    {
        if (event.getWorld() instanceof World)
        {
            World world = (World)event.getWorld();
            if (!world.isRemote)
            {
                DimensionId dimensionId = DimensionId.from(world.getDimensionKey());
                if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
            }
        }
    }
    //endregion
    //region Canceled Events
    @SubscribeEvent public static void drawHighlight(final DrawHighlightEvent event) { if (Active) event.setCanceled(true); }
    @SubscribeEvent public static void onBreakBlock(final BlockEvent.BreakEvent event) { if (Active) event.setCanceled(true); }
    @SubscribeEvent public static void onPlaceBlock(final BlockEvent.EntityPlaceEvent event) { if (Active) event.setCanceled(true); }
    @SubscribeEvent public static void onPlaceBlocks(final BlockEvent.EntityMultiPlaceEvent event) { if (Active) event.setCanceled(true); }
    //endregion
}
