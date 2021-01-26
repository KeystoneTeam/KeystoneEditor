package keystone.api;

import keystone.api.filters.FilterBox;
import keystone.api.filters.KeystoneFilter;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.tools.interfaces.IKeystoneTool;
import keystone.api.tools.interfaces.ISelectionBoxTool;
import keystone.core.KeystoneConfig;
import keystone.core.filters.FilterCache;
import keystone.core.filters.FilterCompiler;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.IKeystoneModule;
import keystone.modules.history.HistoryModule;
import keystone.modules.history.IHistoryEntry;
import keystone.modules.history.entries.WorldBlocksHistoryEntry;
import keystone.modules.selection.SelectionModule;
import keystone.modules.world_cache.WorldCacheModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();

    //region Active Toggle
    public static boolean CloseSelection = false;
    public static boolean RenderHighlightBox = true;
    public static boolean AllowPlayerLook = false;

    private static boolean enabled = KeystoneConfig.startActive;
    private static GameType previousGamemode;
    private static boolean revertGamemode;

    public static void toggleKeystone()
    {
        if (enabled)
        {
            enabled = false;
            Minecraft.getInstance().mouseHelper.grabMouse();
            revertGamemode = true;
        }
        else
        {
            enabled = true;
            AllowPlayerLook = false;
            Minecraft.getInstance().mouseHelper.ungrabMouse();
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
    public static void runFilter(String filterPath)
    {
        KeystoneFilter filter = FilterCompiler.compileFilter(filterPath);
        if (filter != null)
        {
            DimensionId dimensionId = Player.getDimensionId();
            World world = getModule(WorldCacheModule.class).getDimensionWorld(dimensionId);
            if (world == null)
            {
                LOGGER.error("Trying to run keystone tool when there is no loaded world for dimension '" + dimensionId.getDimensionType().getRegistryName() + "'!");
                return;
            }

            SelectionBox[] selectionBoxes = getModule(SelectionModule.class).buildSelectionBoxes(world);
            FilterBox[] boxes = new FilterBox[selectionBoxes.length];
            for (int i = 0; i < boxes.length; i++) boxes[i] = new FilterBox(selectionBoxes[i]);

            Set<BlockPos> processedBlocks = new HashSet<>();
            for (FilterBox box : boxes)
            {
                filter.processBox(box);
                box.forEachBlock((x, y, z) ->
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!filter.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                    {
                        filter.processBlock(x, y, z, box);
                        processedBlocks.add(pos);
                    }
                });
            }

            IHistoryEntry toolHistoryEntry = new WorldBlocksHistoryEntry(world, boxes);
            getModule(HistoryModule.class).pushToHistory(toolHistoryEntry);
            toolHistoryEntry.redo();

            FilterCache.clear();
        }
    }
    //endregion
    //region Event Handling
    public static final void init()
    {
        MinecraftForge.EVENT_BUS.addListener(Keystone::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onRightClickBlock);
    }

    private static final void onPlayerTick(final TickEvent.PlayerTickEvent event)
    {
        ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;

        if (Keystone.isActive())
        {
            if (event.player instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity)event.player;
                if (serverPlayer.getUniqueID().equals(clientPlayer.getUniqueID()))
                {
                    if (serverPlayer.interactionManager.getGameType() != GameType.SPECTATOR)
                    {
                        previousGamemode = serverPlayer.interactionManager.getGameType();
                        serverPlayer.setGameType(GameType.SPECTATOR);
                    }
                }
            }
        }
        else if (revertGamemode)
        {
            if (event.player instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity)event.player;
                if (serverPlayer.getUniqueID().equals(clientPlayer.getUniqueID()))
                {
                    if (previousGamemode != null) serverPlayer.setGameType(previousGamemode);
                    revertGamemode = false;
                }
            }
        }
    }
    private static final void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {
        if (Keystone.isActive()) event.setCanceled(true);
    }
    //endregion
}
