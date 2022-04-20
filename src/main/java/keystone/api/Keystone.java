package keystone.api;

import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.events.minecraft.ClientPlayerEvents;
import keystone.core.events.minecraft.ServerPlayerEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.filter.FilterModule;
import keystone.core.registries.BlockTypeRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base Keystone API class, used to retrieve {@link keystone.core.modules.IKeystoneModule Modules},
 * global state, and toggle whether Keystone is active. Also contains
 * {@link org.apache.logging.log4j.Logger} and {@link java.util.Random} instances
 */
public final class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Random RANDOM = new Random();

    private static FilterModule filterModule;
    private static Thread serverThread;

    //region Active Toggle
    private static boolean enabled;
    private static boolean revertGamemode;
    private static int revertGuiScale;

    /**
     * Toggle whether Keystone is enabled
     */
    public static void toggleKeystone()
    {
        if (enabled) disableKeystone();
        else enableKeystone();
    }
    /**
     * Enable Keystone if it isn't already
     */
    public static void enableKeystone()
    {
        if (enabled) return;
        MinecraftClient minecraft = MinecraftClient.getInstance();

        enabled = true;
        KeystoneGlobalState.AllowPlayerLook = false;
        minecraft.mouse.unlockCursor();
        minecraft.player.getAbilities().setFlySpeed(KeystoneConfig.flySpeed);

        revertGuiScale = minecraft.options.guiScale;
        minecraft.options.guiScale = 3;
        minecraft.onResolutionChanged();
        KeystoneOverlayHandler.resize(minecraft, minecraft.getWindow().getScaledWidth(), minecraft.getWindow().getScaledHeight());
    }
    /**
     * Disable Keystone if it isn't already
     */
    public static void disableKeystone()
    {
        if (!enabled) return;
        MinecraftClient minecraft = MinecraftClient.getInstance();

        enabled = false;
        minecraft.mouse.lockCursor();
        revertGamemode = true;

        minecraft.options.guiScale = revertGuiScale;
        minecraft.onResolutionChanged();
        KeystoneOverlayHandler.resize(minecraft, minecraft.getWindow().getScaledWidth(), minecraft.getWindow().getScaledHeight());
    }
    /**
     * @return If Keystone is active and a world is loaded
     */
    public static boolean isActive()
    {
        return enabled && MinecraftClient.getInstance().world != null;
    }
    //endregion
    //region Module Registry
    private static final Map<Class<? extends IKeystoneModule>, IKeystoneModule> modules = new HashMap<>();

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

    private static final List<DelayedRunnable> runOnMainThread = new ArrayList<>();
    private static final List<DelayedRunnable> addList = new ArrayList<>();

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
    //region Internal Filters
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Run an internal, hard-coded filter on the current selection boxes
     * @param filter The {@link KeystoneFilter} to run
     */
    public static void runInternalFilter(KeystoneFilter filter)
    {
        runInternalFilter(filter, 0);
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Run an internal, hard-coded filter on the current selection boxes after a given tick delay
     * @param filter The {@link KeystoneFilter} to run
     * @param ticksDelay The amount of ticks to wait before running the filter
     */
    public static void runInternalFilter(KeystoneFilter filter, int ticksDelay)
    {
        filterModule.runFilter(filter.compiledSuccessfully().setName(filter.getClass().getSimpleName()), ticksDelay);
    }
    //endregion
    //region API
    /**
     * Cancel filter execution and log the reason
     * @param reason The reason for canceling filter execution
     */
    public static void abortFilter(String... reason) { filterModule.abortFilter(reason); }
    /**
     * Report an exception raised by a filter and log it to the player
     * @param filter The {@link KeystoneFilter} which raised the exception
     * @param throwable The Throwable that was raised
     */
    public static void filterException(KeystoneFilter filter, Throwable throwable) { filterModule.filterException(filter, throwable); }
    //endregion
    //region Event Handling
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Initialize Keystone. Ran once when the mod setup event is called in {@link keystone.core.KeystoneMod}
     */
    public static void init()
    {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context ->
        {
            Player.update(context.tickDelta(), MinecraftClient.getInstance().player);

            if (Keystone.isActive())
            {
                for (IKeystoneModule module : modules.values()) if (module.isEnabled()) module.preRender(context);
                for (IKeystoneModule module : modules.values())
                {
                    module.alwaysRender(context);
                    if (module.isEnabled()) module.renderWhenEnabled(context);
                }
            }
        });

        ClientPlayerEvents.ALLOW_USE_BLOCK.register((player, world, hand, hitResult) -> !Keystone.isActive());

        ServerTickEvents.START_SERVER_TICK.register(Keystone::onServerTick);
        ServerPlayerEvents.START_TICK.register(Keystone::onPlayerTick);
        ServerPlayerEvents.ALLOW_USE_BLOCK.register((player, world, stack, hand, hitResult) -> !Keystone.isActive());

        BlockTypeRegistry.buildRegistry();
        BlockMask.buildForcedAdditionsList();
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Post-initialize keystone. Ran once after all modules have been registered
     */
    public static void postInit()
    {
        filterModule = getModule(FilterModule.class);
        for (IKeystoneModule module : modules.values()) module.postInit();
        KeystoneOverlayHandler.addOverlay(new KeystoneHotbar());
    }
    /**
     * Ran every server tick. Used to execute scheduled {@link keystone.api.Keystone.DelayedRunnable DelayedRunnables}
     * on the server thread
     * @param server The MinecraftServer that is ticking
     */
    private static void onServerTick(MinecraftServer server)
    {
        if (Keystone.isActive())
        {
            serverThread = Thread.currentThread();

            runOnMainThread.addAll(addList);
            addList.clear();

            for (DelayedRunnable runnable : runOnMainThread) runnable.tick();
            runOnMainThread.removeIf(DelayedRunnable::executed);
        }
    }
    /**
     * Ran every time a server player ticks. Used to change the player's gamemode when Keystone is toggled
     * on the server thread
     * @param player The ServerPlayerEntity that is ticking
     */
    private static void onPlayerTick(ServerPlayerEntity player)
    {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        if (clientPlayer == null) return;

        if (Keystone.isActive())
        {
            if (player.getUuid().equals(clientPlayer.getUuid()))
            {
                if (player.interactionManager.getGameMode() != GameMode.SPECTATOR)
                {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
            }
        }
        else if (revertGamemode)
        {
            if (player.getUuid().equals(clientPlayer.getUuid()))
            {
                if (player.interactionManager.getPreviousGameMode().getId() != -1) player.changeGameMode(player.interactionManager.getPreviousGameMode());
                revertGamemode = false;
            }
        }
    }
    //endregion
}
