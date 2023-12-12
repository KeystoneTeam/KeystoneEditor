package keystone.api;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.enums.WorldType;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.events.minecraft.ClientPlayerEvents;
import keystone.core.events.minecraft.ServerPlayerEvents;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.filter.FilterModule;
import keystone.core.modules.filter.execution.IFilterThread;
import keystone.core.modules.rendering.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.world.change_queue.WorldChangeQueueModule;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.renderer.ShapeRenderers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base Keystone API class, used to retrieve {@link keystone.core.modules.IKeystoneModule Modules},
 * global state, and toggle whether Keystone is active. Also contains
 * {@link org.apache.logging.log4j.Logger} and {@link Random} instances
 */
public final class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final java.util.Random RANDOM = new java.util.Random();
    public static final String API_VERSION = "1.0";

    private static FilterModule filterModule;
    private static GhostBlocksModule ghostBlocksModule;
    private static Thread serverThread;

    //region Active Toggle
    private static boolean enabled;
    private static boolean revertPlayerGamemode;
    private static float flySpeed = KeystoneConfig.flySpeed;

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
        // Ensure Keystone isn't already enabled
        if (enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();

        // If Keystone is supported
        if (WorldType.get().canEnableKeystone(true))
        {
            // Enable Keystone
            enabled = true;
            KeystoneGlobalState.AllowPlayerLook = false;
            client.mouse.unlockCursor();
            client.onResolutionChanged();
            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
        }
    }
    /**
     * Disable Keystone if it isn't already
     */
    public static void disableKeystone()
    {
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();

        enabled = false;
        client.mouse.lockCursor();
        revertPlayerGamemode = true;

        client.onResolutionChanged();
    }
    /**
     * @return If Keystone is enabled, a world is loaded, and Keystone is not waiting for
     * {@link WorldChangeQueueModule} changes to finish
     */
    public static boolean isActive()
    {
        return isEnabled() && !KeystoneGlobalState.WaitingForChangeQueue;
    }
    /**
     * @return If Keystone is enabled and a world is loaded
     */
    public static boolean isEnabled()
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
        else
        {
            StringBuilder error = new StringBuilder("Trying to get unregistered Keystone module '");
            error.append(clazz.getSimpleName());
            error.append("'!\n");
            for (StackTraceElement traceElement : Thread.currentThread().getStackTrace()) error.append(traceElement).append('\n');
            LOGGER.error(error);
        }
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
        private final Runnable runnable;
        private int delay;
        private boolean executed;
        private boolean tickWhileNotActive;

        /**
         * @param delay The amount of ticks to delay until running
         * @param runnable The {@link java.lang.Runnable} to run after the delay
         */
        public DelayedRunnable(int delay, Runnable runnable)
        {
            this.delay = delay;
            this.runnable = runnable;
            this.executed = false;
            this.tickWhileNotActive = false;
        }
        public DelayedRunnable tickWhileNotActive() { return setTickWhileNotActive(true); }
        public DelayedRunnable setTickWhileNotActive(boolean tickWhileNotActive)
        {
            this.tickWhileNotActive = tickWhileNotActive;
            return this;
        }
        
        /**
         * Executed once per tick to track timing
         */
        public void tick()
        {
            if (tickWhileNotActive || Keystone.isActive())
            {
                if (delay <= 0)
                {
                    runnable.run();
                    executed = true;
                }
                else delay--;
            }
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

    /**
     * Schedule a {@link Runnable} to run on a Timer thread after a given delay
     * @param delay The delay, in ticks
     * @param runnable The {@link Runnable} to run after the delay
     */
    public static void runDelayed(int delay, Runnable runnable)
    {
        if (delay > 0)
        {
            final Timer t = new Timer();
            t.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    runnable.run();
                    t.cancel();
                }
            }, 50L * delay);
        }
        else runnable.run();
    }
    //endregion
    //region Internal Filters
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Run one or more internal, hard-coded filters on the current selection boxes. All filters
     * will be performed on the same history entry
     * @param filters The {@link KeystoneFilter} to run
     */
    public static void runInternalFilters(KeystoneFilter... filters)
    {
        runInternalFilters(0, filters);
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Run one or more internal, hard-coded filters on the current selection boxes after a given tick delay.
     * All filters will be performed on the same history entry
     * @param ticksDelay The amount of ticks to wait before running the filter
     * @param filters The {@link KeystoneFilter KeystoneFilters} to run
     */
    public static void runInternalFilters(int ticksDelay, KeystoneFilter... filters)
    {
        KeystoneFilter[] renamedFilters = new KeystoneFilter[filters.length];
        for (int i = 0; i < filters.length; i++) renamedFilters[i] = filters[i].compiledSuccessfully().setName(filters[i].getClass().getSimpleName());
        filterModule.runFilters(ticksDelay, renamedFilters);
    }
    //endregion
    //region API
    /**
     * @return The camera fly speed
     */
    public static float getFlySpeed()
    {
        return flySpeed;
    }
    /**
     * Set the camera's fly speed
     * @param speed The new speed of the camera
     */
    public static void setFlySpeed(float speed)
    {
        flySpeed = speed;
    }
    /**
     * Increase the camera's fly speed
     * @param amount The amount to increase the camera's speed by
     */
    public static void increaseFlySpeed(float amount)
    {
        flySpeed += amount;
        flySpeed = Math.min(0.5f, flySpeed);
    }
    /**
     * Decrease the camera's fly speed
     * @param amount The amount to decrease the camera's speed by
     */
    public static void decreaseFlySpeed(float amount)
    {
        flySpeed -= amount;
        flySpeed = Math.max(0, flySpeed);
    }
    
    /**
     * Try to throw an exception from within a {@link KeystoneFilter}
     * @param exception The exception to throw
     * @return True if this method was called from within a filter thread, false otherwise
     */
    public static boolean tryThrowFilterException(Throwable exception)
    {
        if (Thread.currentThread() instanceof IFilterThread filterThread)
        {
            filterThread.getExecutor().throwException(exception);
            return true;
        }
        else
        {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            player.sendMessage(Text.literal(exception.getMessage()).styled(style -> style.withColor(Formatting.RED)), false);
            return false;
        }
    }
    /**
     * Try to cancel the {@link KeystoneFilter} that is being run in the current thread
     * @param reason The reason the filter was canceled
     * @return True if this method was called from within a filter thread, false otherwise
     */
    public static boolean tryCancelFilter(String... reason)
    {
        if (Thread.currentThread() instanceof IFilterThread filterThread)
        {
            filterThread.getExecutor().cancel(reason);
            return true;
        }
        else
        {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            for (String s : reason) player.sendMessage(Text.literal(s).styled(style -> style.withColor(Formatting.RED)), false);
            return false;
        }
    }
    //endregion
    //region Event Handling
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Initialize Keystone. Ran once when the mod setup event is called in {@link keystone.core.KeystoneMod}
     */
    public static void init()
    {
        WorldRenderEvents.LAST.register(context ->
        {
            // Update Client Player Data
            Player.update(context.tickDelta(), MinecraftClient.getInstance().player);
            
            if (Keystone.isEnabled())
            {
                // Fix Render System model view matrix
                RenderSystem.getModelViewStack().push();
                RenderSystem.getModelViewStack().peek().getPositionMatrix().set(context.matrixStack().peek().getPositionMatrix());
                RenderSystem.getModelViewStack().peek().getNormalMatrix().set(context.matrixStack().peek().getNormalMatrix());
                
                // Begin Shape Rendering
                ShapeRenderers.beginRender();
                
                // Render Ghost Blocks
                ghostBlocksModule.renderGhostBlocks(context);
                
                // Pre-Render Enabled Modules
                for (IKeystoneModule module : modules.values()) if (module.isEnabled()) module.preRender(context);
                
                // Render Modules
                for (IKeystoneModule module : modules.values())
                {
                    module.alwaysRender(context);
                    if (module.isEnabled()) module.renderWhenEnabled(context);
                }
                
                // End Shape Rendering
                ShapeRenderers.endRender();
                
                // Revert model view matrix
                RenderSystem.getModelViewStack().pop();
            }
        });

        ClientPlayerEvents.ALLOW_USE_BLOCK.register((player, hand, hitResult) -> !Keystone.isActive());

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
        ghostBlocksModule = getModule(GhostBlocksModule.class);
        for (IKeystoneModule module : modules.values()) module.postInit();
    }
    /**
     * Ran every server tick. Used to execute scheduled {@link keystone.api.Keystone.DelayedRunnable DelayedRunnables}
     * on the server thread
     * @param server The MinecraftServer that is ticking
     */
    private static void onServerTick(MinecraftServer server)
    {
        if (Keystone.isEnabled())
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
        
        if (Keystone.isEnabled())
        {
            if (player.getAbilities().getFlySpeed() != flySpeed)
            {
                player.getAbilities().setFlySpeed(flySpeed);
                MinecraftClient.getInstance().player.getAbilities().setFlySpeed(flySpeed);
            }
            if (player.interactionManager.getGameMode() != GameMode.SPECTATOR) player.changeGameMode(GameMode.SPECTATOR);
        }
        else if (revertPlayerGamemode)
        {
            if (player.getUuid().equals(clientPlayer.getUuid()))
            {
                player.getAbilities().setFlySpeed(0.05f);
                MinecraftClient.getInstance().player.getAbilities().setFlySpeed(0.05f);

                GameMode revertGameMode = MinecraftClient.getInstance().interactionManager.getPreviousGameMode();
                if (revertGameMode == null || revertGameMode.getId() < 0) revertGameMode = player.getServer().getDefaultGameMode();
                if (revertGameMode == null || revertGameMode.getId() < 0) revertGameMode = GameMode.CREATIVE;
                player.changeGameMode(revertGameMode);
                revertPlayerGamemode = false;
            }
        }
    }
    //endregion
}
