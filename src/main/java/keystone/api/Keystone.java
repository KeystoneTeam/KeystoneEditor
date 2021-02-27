package keystone.api;

import keystone.api.filters.FilterBox;
import keystone.api.filters.KeystoneFilter;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.tools.interfaces.IKeystoneTool;
import keystone.api.tools.interfaces.ISelectionBoxTool;
import keystone.api.wrappers.Block;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.filters.FilterCompiler;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.DimensionId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base Keystone API class, used to retrieve {@link keystone.core.modules.IKeystoneModule Modules},
 * retrieve global state, and toggle whether Keystone is active. Also contains
 * {@link org.apache.logging.log4j.Logger} and {@link java.util.Random} instance
 */
public class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Random RANDOM = new Random();

    private static HistoryModule historyModule;

    //region Active Toggle
    private static boolean enabled = KeystoneConfig.startActive;
    private static GameType previousGamemode;
    private static boolean revertGamemode;

    /**
     * Toggle whether Keystone is enabled
     */
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
            KeystoneGlobalState.AllowPlayerLook = false;
            Minecraft.getInstance().mouseHelper.ungrabMouse();
        }
    }

    /**
     * @return If Keystone is active and a world is loaded
     */
    public static boolean isActive()
    {
        return enabled && Minecraft.getInstance().world != null;
    }
    //endregion
    //region Module Registry
    private static Map<Class, IKeystoneModule> modules = new HashMap<>();

    /**
     * Register a new {@link keystone.core.modules.IKeystoneModule Module}
     * @param module The module to register
     */
    public static void registerModule(IKeystoneModule module)
    {
        if (modules.containsKey(module.getClass())) LOGGER.error("Trying to register keystone module '" + module.getClass().getSimpleName() + "', when it was already registered!");
        else modules.put(module.getClass(), module);
    }
    /**
     * Get a registered {@link keystone.core.modules.IKeystoneModule Module} by class
     * @param clazz The module class to retrieve
     * @param <T> A class implementing {@link keystone.core.modules.IKeystoneModule}
     * @return The module, or null if it is not registered
     */
    public static <T extends IKeystoneModule> T getModule(Class<T> clazz)
    {
        if (modules.containsKey(clazz)) return (T)modules.get(clazz);
        else LOGGER.error("Trying to get unregistered keystone module '" + clazz.getSimpleName() + "'!");
        return null;
    }
    /**
     * Run a function on every {@link keystone.core.modules.IKeystoneModule Module} in the registry
     * @param consumer The function to run
     */
    public static void forEachModule(Consumer<IKeystoneModule> consumer)
    {
        modules.values().forEach(consumer);
    }
    //endregion
    //region Threading
    /**
     * A {@link java.lang.Runnable} that will be executed after a delay
     */
    private static class DelayedRunnable
    {
        private int delay;
        private Runnable runnable;
        private boolean executed;

        /**
         * @param delay The amount of ticks to delay until running
         * @param runnable The {@link java.lang.Runnable} to run after the delay
         */
        public DelayedRunnable(int delay, Runnable runnable)
        {
            this.delay = delay;
            this.runnable = runnable;
            this.executed = false;
        }
        /**
         * Executed once per tick to track timing
         */
        public void tick()
        {
            if (delay <= 0)
            {
                runnable.run();
                executed = true;
            }
            else delay--;
        }
        /**
         * @return Whether the {@link java.lang.Runnable} was executed
         */
        public boolean executed()
        {
            return executed;
        }
    }

    private static List<DelayedRunnable> runOnMainThread = new ArrayList<>();
    private static List<DelayedRunnable> addList = new ArrayList<>();

    /**
     * Schedule a {@link java.lang.Runnable} to run on the server thread next tick
     * @param runnable The {@link java.lang.Runnable} to run on the server thread
     */
    public static void runOnMainThread(Runnable runnable) { runOnMainThread(0, runnable); }

    /**
     * Schedule a {@link java.lang.Runnable} to run on the server thread after a given delay
     * @param delay The delay, in ticks
     * @param runnable The {@link java.lang.Runnable} to run after the delay
     */
    public static void runOnMainThread(int delay, Runnable runnable)
    {
        addList.add(new DelayedRunnable(delay, runnable));
    }
    //endregion
    //region Tools
    private static ITextComponent abortFilter;

    /**
     * Set a {@link keystone.api.wrappers.Block} in the current world. This will automatically hook into the history system, allowing
     * for undo and redo support. Be sure that the {@link keystone.core.modules.history.HistoryModule}
     * has an entry open first
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @param block The {@link keystone.api.wrappers.Block} to set
     */
    public static void setBlock(int x, int y, int z, Block block)
    {
        historyModule.getOpenEntry().setBlock(x, y, z, block);
    }

    /**
     * Run an {@link keystone.api.tools.interfaces.IKeystoneTool} on the current selection boxes
     * @param tool The tool to run
     */
    public static void runTool(IKeystoneTool tool)
    {
        runOnMainThread(() ->
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
                    box.forEachBlock(((pos, block) ->
                    {
                        if (!blockTool.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                        {
                            blockTool.process(pos.getMinecraftBlockPos(), box);
                            processedBlocks.add(pos.getMinecraftBlockPos());
                        }
                    }));
                }
            }

            historyModule.beginHistoryEntry();
            for (SelectionBox box : boxes) box.forEachBlock((pos, block) -> setBlock(pos.x, pos.y, pos.z, block));
            historyModule.endHistoryEntry();
        });
    }

    /**
     * Compile and run a {@link keystone.api.filters.KeystoneFilter} on the current selection boxes
     * @param filterPath The path to the filter file
     */
    public static void runFilter(String filterPath)
    {
        abortFilter = null;

        KeystoneFilter filter = FilterCompiler.compileFilter(filterPath);
        if (abortFilter != null)
        {
            Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
            return;
        }
        else runFilter(filter);
    }
    /**
     * Run a {@link keystone.api.filters.KeystoneFilter} on the current selection boxes
     * @param filter The filter to run
     */
    public static void runFilter(KeystoneFilter filter) { runFilter(filter, 0); }
    /**
     * Run a {@link keystone.api.filters.KeystoneFilter} on the current selection boxes after a delay
     * @param filter The filter to run
     * @param ticksDelay The delay, in ticks
     */
    public static void runFilter(KeystoneFilter filter, int ticksDelay)
    {
        runOnMainThread(ticksDelay, () ->
        {
            abortFilter = null;

            if (filter.isCompiledSuccessfully())
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
                for (int i = 0; i < boxes.length; i++) boxes[i] = new FilterBox(world, selectionBoxes[i], filter);

                filter.setFilterBoxes(boxes);
                filter.prepare();
                if (abortFilter != null)
                {
                    Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                    return;
                }

                Set<BlockPos> processedBlocks = new HashSet<>();
                for (FilterBox box : boxes)
                {
                    filter.processBox(box);
                    if (abortFilter != null)
                    {
                        Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                        return;
                    }

                    box.forEachBlock((x, y, z, block) ->
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!filter.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                        {
                            filter.processBlock(x, y, z, box);
                            processedBlocks.add(pos);
                        }

                        if (abortFilter != null)
                        {
                            Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                            return;
                        }
                    });
                }

                if (abortFilter != null)
                {
                    Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                    return;
                }

                filter.finished();
                if (abortFilter != null)
                {
                    Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                    return;
                }

                historyModule.beginHistoryEntry();
                for (FilterBox box : boxes) box.forEachBlock((x, y, z, block) -> setBlock(x, y, z, block));
                historyModule.endHistoryEntry();
            }
        });
    }

    /**
     * Abort filter execution
     * @param reason The reason for aborting the filter
     */
    public static void abortFilter(String reason)
    {
        abortFilter = new StringTextComponent(reason).mergeStyle(TextFormatting.RED);
    }
    //endregion
    //region Event Handling
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Initialize Keystone. Ran once when the mod setup event is called in {@link keystone.core.KeystoneMod}
     */
    public static final void init()
    {
        MinecraftForge.EVENT_BUS.addListener(Keystone::onWorldTick);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onRightClickBlock);
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Post-initialize keystone. Ran once after all modules have been registered
     */
    public static final void postInit()
    {
        historyModule = getModule(HistoryModule.class);
    }
    /**
     * Ran every world tick. Used to execute scheduled {@link keystone.api.Keystone.DelayedRunnable DelayedRunnables}
     * on the server thread
     * @param event The {@link net.minecraftforge.event.TickEvent.WorldTickEvent} that was posted
     */
    private static final void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        if (Keystone.isActive() && event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER)
        {
            runOnMainThread.addAll(addList);
            addList.clear();

            for (DelayedRunnable runnable : runOnMainThread) runnable.tick();
            runOnMainThread.removeIf(runnable -> runnable.executed());
        }
    }
    /**
     * Ran every player tick. Used to change the player's gamemode when Keystone is toggled
     * on the server thread
     * @param event The {@link net.minecraftforge.event.TickEvent.PlayerTickEvent} that was posted
     */
    private static final void onPlayerTick(final TickEvent.PlayerTickEvent event)
    {
        ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null || event.side != LogicalSide.SERVER) return;

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
    /**
     * Ran when the player right-clicks a block. Used to prevent players opening containers while
     * Keystone is active
     * @param event The {@link net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock} that was posted
     */
    private static final void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {
        if (Keystone.isActive()) event.setCanceled(true);
    }
    //endregion
}
