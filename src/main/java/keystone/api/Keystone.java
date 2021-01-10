package keystone.api;

import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.tools.interfaces.IKeystoneTool;
import keystone.api.tools.interfaces.ISelectionBoxTool;
import keystone.modules.history.IHistoryEntry;
import keystone.modules.history.entries.WorldBlocksHistoryEntry;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.renderer.config.KeystoneConfig;
import keystone.modules.IKeystoneModule;
import keystone.modules.history.HistoryModule;
import keystone.modules.selection.SelectionModule;
import keystone.modules.world_cache.WorldCacheModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();

    //region Active Toggle
    public static boolean CloseSelection = false;
    public static boolean RenderHighlightBox = true;
    public static boolean AllowPlayerLook = false;

    private static boolean enabled = KeystoneConfig.startActive;

    public static void toggleKeystone()
    {
        if (enabled)
        {
            enabled = false;
            Minecraft.getInstance().mouseHelper.grabMouse();
        }
        else
        {
            enabled = true;
            AllowPlayerLook = false;
        }
    }
    public static boolean isActive()
    {
        return enabled && Minecraft.getInstance().world != null;
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
        World world = getModule(WorldCacheModule.class).getDimensionWorld(dimensionId);
        if (world == null)
        {
            LOGGER.error("Trying to run keystone tool when there is no loaded world for dimension '" + dimensionId.getDimensionType().getRegistryName() + "'!");
            return;
        }

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

        IHistoryEntry toolHistoryEntry = new WorldBlocksHistoryEntry(world, boxes);
        getModule(HistoryModule.class).pushToHistory(toolHistoryEntry);
        toolHistoryEntry.redo();
    }
    //endregion
    //region Event Handling
    public static final void init()
    {
        MinecraftForge.EVENT_BUS.addListener(Keystone::drawHighlight);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onBreakBlock);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onPlaceBlock);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onPlaceBlocks);
    }

    private static void drawHighlight(final DrawHighlightEvent event) { if (isActive()) event.setCanceled(true); }
    private static void onBreakBlock(final BlockEvent.BreakEvent event) { if (isActive()) event.setCanceled(true); }
    private static void onPlaceBlock(final BlockEvent.EntityPlaceEvent event) { if (isActive()) event.setCanceled(true); }
    private static void onPlaceBlocks(final BlockEvent.EntityMultiPlaceEvent event) { if (isActive()) event.setCanceled(true); }
    //endregion
}
