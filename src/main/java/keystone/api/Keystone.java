package keystone.api;

import keystone.api.filters.KeystoneFilter;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.tools.interfaces.IKeystoneTool;
import keystone.api.tools.interfaces.ISelectionBoxTool;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.modules.filter.FilterCompiler;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.filter.FilterModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * Base Keystone API class, used to retrieve {@link keystone.core.modules.IKeystoneModule Modules},
 * retrieve global state, and toggle whether Keystone is active. Also contains
 * {@link org.apache.logging.log4j.Logger} and {@link java.util.Random} instance
 */
public final class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Random RANDOM = new Random();

    private static HistoryModule historyModule;
    private static FilterModule filterModule;
    private static Thread serverThread;

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
        if (delay == 0 && Thread.currentThread() == serverThread) runnable.run();
        else addList.add(new DelayedRunnable(delay, runnable));
    }
    //endregion
    //region Tools
    /**
     * Run an {@link keystone.api.tools.interfaces.IKeystoneTool} on the current selection boxes
     * @param tool The tool to run
     */
    public static void runTool(IKeystoneTool tool)
    {
        runOnMainThread(() ->
        {
            historyModule.tryBeginHistoryEntry();
            BlockRegion[] regions = getModule(SelectionModule.class).buildRegions(false);

            Set<BlockPos> processedBlocks = new HashSet<>();
            for (BlockRegion region : regions)
            {
                if (tool instanceof ISelectionBoxTool) ((ISelectionBoxTool)tool).process(region);
                if (tool instanceof IBlockTool)
                {
                    IBlockTool blockTool = (IBlockTool)tool;
                    region.forEachBlock(((x, y, z, block) ->
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!blockTool.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                        {
                            blockTool.process(x, y, z, region);
                            processedBlocks.add(pos);
                        }
                    }));
                }
            }
            historyModule.tryEndHistoryEntry();
        });
    }
    //endregion
    //region API
    public static void abortFilter(String... reason) { filterModule.abortFilter(reason); }
    public static void filterException(KeystoneFilter filter, Exception e) { filterModule.filterException(filter, e); }
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
        filterModule = getModule(FilterModule.class);
        for (IKeystoneModule module : modules.values()) module.postInit();
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
            serverThread = Thread.currentThread();

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
