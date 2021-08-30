package keystone.core.modules.brush;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.brush.boxes.BrushPositionBox;
import keystone.core.modules.brush.providers.BrushPositionBoxProvider;
import keystone.core.modules.brush.providers.BrushPreviewBoxProvider;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BrushModule implements IKeystoneModule
{
    public static final int IMMEDIATE_MODE_COOLDOWN_TICKS = 5;

    private HistoryModule historyModule;
    private WorldModifierModules worldModifiers;

    private IBoundingBoxProvider[] providers;
    private BrushOperation brushOperation;
    private BrushShape brushShape;
    private int[] brushSize;
    private int minSpacing;
    private float minSpacingSqr;
    private int noise;
    private List<Coords> brushPositions = new ArrayList<>();
    private List<BrushPositionBox> brushPositionBoxes = new ArrayList<>();

    private boolean immediateMode;
    private Coords immediateModePosition;
    private Coords lastImmediateModePosition;
    private int immediateModeCooldown;
    private ShapeMask immediateModeShapeMask;
    private List<Coords> lastImmediateModeChanges = new ArrayList<>();

    private boolean dragging = false;
    private Coords lastCheckedPosition;

    public BrushModule()
    {
        providers = new IBoundingBoxProvider[]
        {
                new BrushPositionBoxProvider(),
                new BrushPreviewBoxProvider()
        };

        MinecraftForge.EVENT_BUS.register(this);
        setMinSpacing(1);
        setNoise(100);
        setBrushShape(BrushShape.ROUND);
        setBrushOperation(BrushOperation.FILL);
        setBrushSize(9, 9, 9);
        setImmediateMode(false);
    }

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        worldModifiers = new WorldModifierModules();
    }
    @Override
    public void resetModule()
    {
        brushPositions.clear();
        brushPositionBoxes.clear();
        lastImmediateModeChanges.clear();

        setMinSpacing(1);
        setNoise(100);
        setBrushShape(BrushShape.ROUND);
        setBrushOperation(BrushOperation.FILL);
        setBrushSize(9, 9, 9);
        setImmediateMode(false);
    }
    @Override
    public boolean isEnabled() { return KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.BRUSH; }

    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return providers;
    }

    public BrushOperation getBrushOperation() { return brushOperation; }
    public boolean isImmediateMode() { return immediateMode; }
    public BrushShape getBrushShape() { return brushShape; }
    public int[] getBrushSize() { return brushSize; }
    public int getMinSpacing() { return minSpacing; }
    public int getNoise() { return noise; }
    public List<BrushPositionBox> getBrushPositionBoxes() { return brushPositionBoxes; }

    //region Event Handlers
    @SubscribeEvent
    public final void onMouseClick(final KeystoneInputEvent.MouseClickEvent event)
    {
        if (!Keystone.isActive() || !isEnabled() || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            prepareBrush();
            addBrushPosition(Player.getHighlightedBlock(), true);
            executeBrush(true, true);
        }
    }
    @SubscribeEvent
    public final void onMouseDragStart(final KeystoneInputEvent.MouseDragStartEvent event)
    {
        if (!Keystone.isActive() || !isEnabled() || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            prepareBrush();
            addBrushPosition(Player.getHighlightedBlock(), true);
            dragging = true;
            executeBrush(true, false);
        }
    }
    @SubscribeEvent
    public final void onRender(final RenderWorldLastEvent event)
    {
        if (dragging)
        {
            Coords pos = Player.getHighlightedBlock();

            if (immediateMode)
            {
                if (immediateModeCooldown <= 0 || (KeystoneGlobalState.CloseSelection && pos != lastCheckedPosition))
                {
                    if (addBrushPosition(pos)) if (immediateMode) executeBrush(false, false);
                    lastCheckedPosition = pos;
                }
            }
            else
            {
                if (pos != lastCheckedPosition)
                {
                    addBrushPosition(pos);
                    lastCheckedPosition = pos;
                }
            }
        }
    }
    @SubscribeEvent
    public final void onTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START) if (immediateModeCooldown > 0) immediateModeCooldown--;
    }
    @SubscribeEvent
    public final void onMouseDragEnd(final KeystoneInputEvent.MouseDragEndEvent event)
    {
        if (!Keystone.isActive() || !isEnabled() || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (!immediateMode || addBrushPosition(Player.getHighlightedBlock())) executeBrush(false, true);
            dragging = false;
        }
    }
    //endregion
    //region Brush Functions
    public void setBrushOperation(BrushOperation operation)
    {
        this.brushOperation = operation;
    }
    public void setImmediateMode(boolean immediateMode)
    {
        this.immediateMode = immediateMode;
    }
    public void setBrushShape(BrushShape shape)
    {
        this.brushShape = shape;
    }
    public void setBrushSize(int x, int y, int z)
    {
        if (x <= 0) x = 1;
        if (y <= 0) y = 1;
        if (z <= 0) z = 1;
        brushSize = new int[] { x, y, z };
    }
    public void setMinSpacing(int minSpacing)
    {
        this.minSpacingSqr = minSpacing * minSpacing;
        this.minSpacing = minSpacing;
    }
    public void setNoise(int noise)
    {
        this.noise = noise;
    }

    public void prepareBrush()
    {
        brushPositions.clear();
        brushPositionBoxes.clear();
        immediateModePosition = null;
        lastImmediateModePosition = null;
        immediateModeShapeMask = immediateMode ? this.brushShape.getShapeMask(brushSize[0], brushSize[1], brushSize[2]) : null;
    }

    public boolean addBrushPosition(Coords position) { return addBrushPosition(position, false); }
    public boolean addBrushPosition(Coords position, boolean force)
    {
        if (position == null) return false;

        if (force || getPositionSpacingSqr(position) >= minSpacingSqr)
        {
            if (immediateMode) immediateModePosition = position;
            else
            {
                brushPositions.add(position);
                brushPositionBoxes.add(new BrushPositionBox(position));
            }

            return true;
        }
        else return false;
    }
    //endregion
    //region Brush Execution
    public void executeBrush(boolean starting, boolean ending)
    {
        Keystone.runOnMainThread(() ->
        {
            if (starting)
            {
                if (worldModifiers.blocks.isEnabled()) historyModule.beginHistoryEntry();
                else
                {
                    brushPositions.clear();
                    return;
                }
            }

            if (immediateMode)
            {
                immediateModeExecute();
                historyModule.applyBlocksWithoutEnding();
            }
            else deferredModeExecute();

            if (ending) historyModule.endHistoryEntry();
        });
    }

    private void immediateModeExecute()
    {
        lastImmediateModeChanges.clear();
        List<BlockPos> processedBlocks = new ArrayList<>();

        int iterations = brushOperation.iterations();
        for (int iteration = 0; iteration < iterations; iteration++)
        {
            Coords min = immediateModePosition.sub(brushSize[0] / 2, brushSize[1] / 2, brushSize[2] / 2);
            Coords max = min.add(brushSize[0] - 1, brushSize[1] - 1, brushSize[2] - 1);
            executeBrush(new BlockPos(min.getX(), min.getY(), min.getZ()), new BlockPos(max.getX(), max.getY(), max.getZ()), processedBlocks, immediateModeShapeMask, iteration);
            if (iteration < iterations - 1) worldModifiers.blocks.swapBuffers(true);
        }

        lastImmediateModePosition = immediateModePosition;
        immediateModeCooldown = IMMEDIATE_MODE_COOLDOWN_TICKS;
    }
    private void deferredModeExecute()
    {
        ShapeMask shapeMask = this.brushShape.getShapeMask(brushSize[0], brushSize[1], brushSize[2]);
        List<BlockPos> processedBlocks = new ArrayList<>();

        int iterations = brushOperation.iterations();
        for (int iteration = 0; iteration < iterations; iteration++)
        {
            for (Coords position : brushPositions)
            {
                Coords min = position.sub(brushSize[0] / 2, brushSize[1] / 2, brushSize[2] / 2);
                Coords max = min.add(brushSize[0] - 1, brushSize[1] - 1, brushSize[2] - 1);
                executeBrush(new BlockPos(min.getX(), min.getY(), min.getZ()), new BlockPos(max.getX(), max.getY(), max.getZ()), processedBlocks, shapeMask, iteration);
            }
            processedBlocks.clear();
            if (iteration < iterations - 1) worldModifiers.blocks.swapBuffers(true);
        }

        brushPositions.clear();
        brushPositionBoxes.clear();
    }
    private void executeBrush(BlockPos min, BlockPos max, List<BlockPos> processedBlocks, ShapeMask shapeMask, int iteration)
    {
        for (int y = min.getY(); y <= max.getY(); y++)
        {
            for (int x = min.getX(); x <= max.getX(); x++)
            {
                for (int z = min.getZ(); z <= max.getZ(); z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockPos nPos = pos.subtract(min);

                    if (!processedBlocks.contains(pos) && shapeMask.test(nPos.getX(), nPos.getY(), nPos.getZ()))
                    {
                        if (brushOperation.process(x, y, z, worldModifiers, iteration)) processedBlocks.add(pos);
                    }
                }
            }
        }
    }
    //endregion
    //region Helpers
    private float getPositionSpacingSqr(Coords position)
    {
        if (immediateMode)
        {
            if (lastImmediateModePosition != null)
            {
                if (lastImmediateModePosition.equals(position)) return minSpacingSqr + 1;
                else return position.distanceSqr(lastImmediateModePosition);
            }
            else return minSpacingSqr;
        }
        else
        {
            float dist = -1;
            for (Coords test : brushPositions)
            {
                float testDist = position.distanceSqr(test);
                if (dist < 0 || testDist < dist) dist = testDist;
            }
            if (dist < 0) dist = minSpacingSqr;
            return dist;
        }
    }
    //endregion
}
