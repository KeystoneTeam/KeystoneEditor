package keystone.core.modules.brush;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.events.keystone.KeystoneInputEvents;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.RendererFactory;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import keystone.core.utils.ProgressBar;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrushModule implements IKeystoneModule
{
    public static final int IMMEDIATE_MODE_COOLDOWN_TICKS = 5;

    private HistoryModule historyModule;
    private WorldModifierModules worldModifiers;

    private ComplexOverlayRenderer outsideRenderer;
    private ComplexOverlayRenderer insideRenderer;

    private BrushOperation brushOperation;
    private BrushShape brushShape;
    private int[] brushSize;
    private int minSpacing;
    private float minSpacingSqr;
    private int noise;
    private final List<Vec3i> brushPositions = Collections.synchronizedList(new ArrayList<>());

    private boolean immediateMode;
    private Vec3i immediateModePosition;
    private Vec3i lastImmediateModePosition;
    private int immediateModeCooldown;
    private ShapeMask immediateModeShapeMask;
    private final List<Vec3i> lastImmediateModeChanges = new ArrayList<>();

    private boolean dragging = false;
    private boolean executionCancelled = false;
    private Vec3i lastCheckedPosition;

    public BrushModule()
    {
        setMinSpacing(1);
        setNoise(100);
        setBrushShape(BrushShape.ROUND);
        setBrushOperation(BrushOperation.FILL);
        setBrushSize(9, 9, 9);
        setImmediateMode(false);

        KeystoneInputEvents.MOUSE_CLICKED.register(this::onMouseClick);
        KeystoneInputEvents.START_MOUSE_DRAG.register(this::onMouseDragStart);
        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
        KeystoneInputEvents.END_MOUSE_DRAG.register(this::onMouseDragEnd);

        this.outsideRenderer = RendererFactory.createComplexOverlay(
                RendererFactory.createWorldspaceOverlay().buildFill(),
                RendererFactory.createWorldspaceOverlay().ignoreDepth().buildWireframe()
        );
        this.insideRenderer = RendererFactory.createComplexOverlay(
                RendererFactory.createWorldspaceOverlay().ignoreCull().buildFill(),
                RendererFactory.createWorldspaceOverlay().ignoreDepth().buildWireframe()
        );
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
    public void preRender(WorldRenderContext context)
    {
        if (dragging)
        {
            Vec3i pos = Player.getHighlightedBlock();

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

    @Override
    public void renderWhenEnabled(WorldRenderContext context)
    {
        // Render Brush Positions
        this.outsideRenderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME);
        for (Vec3i brushPosition : brushPositions)
        {
            RenderBox box = new RenderBox(brushPosition).nudge();
            this.outsideRenderer.drawCuboid(box, Color4f.yellow);
        }

        // Render Brush Preview
        Vec3d center = Vec3d.of(Player.getHighlightedBlock());
        center = center.add(brushSize[0] % 2 == 1 ? 0.5: 0, brushSize[1] % 2 == 1 ? 0.5: 0, brushSize[2] % 2 == 1 ? 0.5: 0);
        Vec3i centerBlock = new Vec3i(center.x, center.y, center.z);

        double xRadius = brushSize[0] * 0.5;
        double yRadius = brushSize[1] * 0.5;
        double zRadius = brushSize[2] * 0.5;

        boolean outside = !brushShape.isPositionInShape(Player.getEyePosition(), centerBlock, brushSize[0], brushSize[1], brushSize[2]);
        ComplexOverlayRenderer renderer = outside ? outsideRenderer : insideRenderer;
        if (brushShape == BrushShape.ROUND)
        {
            //(outside ? outsideFillRenderer : insideFillRenderer).drawSphere(center, xRadius, yRadius, zRadius, blue, 128, false, false, outside);
        }
        else if (brushShape == BrushShape.DIAMOND)
        {
            //(outside ? outsideFillRenderer : insideFillRenderer).drawDiamond(center, xRadius, yRadius, zRadius, blue, 128, false, false, outside);
        }
        else if (brushShape == BrushShape.SQUARE)
        {
            RenderBox box = new RenderBox(center.add(-xRadius, -yRadius, -zRadius), center.add(xRadius, yRadius, zRadius));
            renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box, Color4f.blue.withAlpha(0.5f));
            renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box, Color4f.blue);
        }
    }

    public BrushOperation getBrushOperation() { return brushOperation; }
    public boolean isImmediateMode() { return immediateMode; }
    public BrushShape getBrushShape() { return brushShape; }
    public int[] getBrushSize() { return brushSize; }
    public int getMinSpacing() { return minSpacing; }
    public int getNoise() { return noise; }

    //region Event Handlers
    private void onMouseClick(int button, int modifiers, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isActive() || !isEnabled() || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            prepareBrush();
            addBrushPosition(Player.getHighlightedBlock(), true);
            executeBrush(true, true);
        }
    }
    private void onMouseDragStart(int button, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isActive() || !isEnabled() || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            prepareBrush();
            addBrushPosition(Player.getHighlightedBlock(), true);
            dragging = true;
            executeBrush(true, false);
        }
    }
    private void onTick(MinecraftClient client)
    {
        if (immediateModeCooldown > 0) immediateModeCooldown--;
    }
    private void onMouseDragEnd(int button, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isActive() || !isEnabled() || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
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
        immediateModePosition = null;
        lastImmediateModePosition = null;
        immediateModeShapeMask = immediateMode ? this.brushShape.getShapeMask(brushSize[0], brushSize[1], brushSize[2]) : null;
    }

    public boolean addBrushPosition(Vec3i position) { return addBrushPosition(position, false); }
    public boolean addBrushPosition(Vec3i position, boolean force)
    {
        if (position == null) return false;

        if (force || getPositionSpacingSqr(position) >= minSpacingSqr)
        {
            if (immediateMode) immediateModePosition = position;
            else
            {
                brushPositions.add(position);
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
                if (ending) historyModule.endHistoryEntry();
            }
            else if (ending)
            {
                if (deferredModeExecute()) historyModule.endHistoryEntry();
                else historyModule.abortHistoryEntry();
            }
        });
    }
    public void cancel()
    {
        executionCancelled = true;
    }

    private void immediateModeExecute()
    {
        executionCancelled = false;
        lastImmediateModeChanges.clear();
        List<BlockPos> processedBlocks = new ArrayList<>();

        int iterations = brushOperation.iterations();
        for (int iteration = 0; iteration < iterations; iteration++)
        {
            Vec3i min = immediateModePosition.subtract(new Vec3i(brushSize[0] >> 1, brushSize[1] >> 1, brushSize[2] >> 1));
            Vec3i max = min.add(brushSize[0] - 1, brushSize[1] - 1, brushSize[2] - 1);
            executeBrush(new BlockPos(min.getX(), min.getY(), min.getZ()), new BlockPos(max.getX(), max.getY(), max.getZ()), processedBlocks, immediateModeShapeMask, iteration, false);
            if (iteration < iterations - 1) worldModifiers.blocks.swapBuffers(true);
        }

        lastImmediateModePosition = immediateModePosition;
        immediateModeCooldown = IMMEDIATE_MODE_COOLDOWN_TICKS;
    }
    private boolean deferredModeExecute()
    {
        executionCancelled = false;
        ShapeMask shapeMask = this.brushShape.getShapeMask(brushSize[0], brushSize[1], brushSize[2]);
        List<BlockPos> processedBlocks = new ArrayList<>();

        int progressBarIterations = brushOperation.iterations() * brushPositions.size();
        boolean progressBarGrouped = progressBarIterations > 16384;
        ProgressBar.start(brushOperation.getName().getString(), progressBarGrouped ? progressBarIterations : 1, this::cancel);
        int iterations = brushOperation.iterations();

        mainLoop:
        for (int iteration = 0; iteration < iterations; iteration++)
        {
            Vec3i[] positions = new Vec3i[brushPositions.size()];
            brushPositions.toArray(positions);
            for (Vec3i position : positions)
            {
                if (executionCancelled) break mainLoop;
                Vec3i min = position.subtract(new Vec3i(brushSize[0] >> 1, brushSize[1] >> 1, brushSize[2] >> 1));
                Vec3i max = min.add(brushSize[0] - 1, brushSize[1] - 1, brushSize[2] - 1);
                executeBrush(new BlockPos(min.getX(), min.getY(), min.getZ()), new BlockPos(max.getX(), max.getY(), max.getZ()), processedBlocks, shapeMask, iteration, progressBarGrouped);
            }

            processedBlocks.clear();
            if (iteration < iterations - 1) worldModifiers.blocks.swapBuffers(true);
        }

        ProgressBar.finish();
        brushPositions.clear();
        return !executionCancelled;
    }
    private void executeBrush(BlockPos min, BlockPos max, List<BlockPos> processedBlocks, ShapeMask shapeMask, int iteration, boolean progressBarGrouped)
    {
        if (!progressBarGrouped) ProgressBar.beginIteration((max.getX() - min.getX()) * (max.getY() - min.getY()) * (max.getZ() - min.getZ()));
        for (int y = min.getY(); y <= max.getY(); y++)
        {
            for (int x = min.getX(); x <= max.getX(); x++)
            {
                for (int z = min.getZ(); z <= max.getZ(); z++)
                {
                    if (executionCancelled) return;
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockPos nPos = pos.subtract(min);

                    if (!processedBlocks.contains(pos) && shapeMask.test(nPos.getX(), nPos.getY(), nPos.getZ()))
                    {
                        if (brushOperation.process(x, y, z, worldModifiers, iteration)) processedBlocks.add(pos);
                    }
                    ProgressBar.nextStep();
                }
            }
        }
        if (!progressBarGrouped) ProgressBar.nextIteration();
    }
    //endregion
    //region Helpers
    private double getPositionSpacingSqr(Vec3i position)
    {
        if (immediateMode)
        {
            if (lastImmediateModePosition != null)
            {
                if (lastImmediateModePosition.equals(position)) return minSpacingSqr + 1;
                else return position.getSquaredDistance(lastImmediateModePosition);
            }
            else return minSpacingSqr;
        }
        else
        {
            double dist = -1;
            for (Vec3i test : brushPositions)
            {
                double testDist = position.getSquaredDistance(test);
                if (dist < 0 || testDist < dist) dist = testDist;
            }
            if (dist < 0) dist = minSpacingSqr;
            return dist;
        }
    }
    //endregion
}
