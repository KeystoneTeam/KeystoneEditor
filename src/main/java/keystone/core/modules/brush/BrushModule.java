package keystone.core.modules.brush;

import keystone.api.Keystone;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.brush.boxes.BrushPositionBox;
import keystone.core.modules.brush.providers.BrushPositionBoxProvider;
import keystone.core.modules.brush.providers.BrushPreviewBoxProvider;
import keystone.core.modules.history.HistoryModule;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BrushModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private BlocksModule blocksModule;

    private IBoundingBoxProvider[] providers;
    private BrushOperation brushOperation;
    private BrushShape brushShape;
    private int[] brushSize;
    private int minSpacing;
    private float minSpacingSqr;
    private int noise;
    private List<Coords> brushPositions = new ArrayList<>();
    private List<BrushPositionBox> brushPositionBoxes = new ArrayList<>();

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
    }

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        blocksModule = Keystone.getModule(BlocksModule.class);
    }
    @Override
    public boolean isEnabled() { return KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.BRUSH; }

    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return providers;
    }

    public BrushOperation getBrushOperation() { return brushOperation; }
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
            executeBrush();
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
        }
    }
    @SubscribeEvent
    public final void onRender(final RenderWorldLastEvent event)
    {
        if (dragging)
        {
            Coords pos = Player.getHighlightedBlock();
            if (pos != lastCheckedPosition)
            {
                addBrushPosition(pos);
                lastCheckedPosition = pos;
            }
        }
    }
    @SubscribeEvent
    public final void onMouseDragEnd(final KeystoneInputEvent.MouseDragEndEvent event)
    {
        if (!Keystone.isActive() || !isEnabled() || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            addBrushPosition(Player.getHighlightedBlock());
            dragging = false;
            executeBrush();
        }
    }
    //endregion
    //region Brush Functions
    public void setBrushOperation(BrushOperation operation)
    {
        this.brushOperation = operation;
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
    }

    public void addBrushPosition(Coords position) { addBrushPosition(position, false); }
    public void addBrushPosition(Coords position, boolean force)
    {
        if (position == null) return;

        if (force || getDistanceToNearestPositionSqr(position) >= minSpacingSqr)
        {
            brushPositions.add(position);
            brushPositionBoxes.add(new BrushPositionBox(position));
        }
    }

    public void executeBrush()
    {
        Keystone.runOnMainThread(() ->
        {
            if (blocksModule.isEnabled()) historyModule.beginHistoryEntry();
            else
            {
                brushPositions.clear();
                return;
            }

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
                if (iteration < iterations - 1) blocksModule.swapBuffers(true);
            }

            brushPositions.clear();
            brushPositionBoxes.clear();
            historyModule.endHistoryEntry();
        });
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
                        if (brushOperation.process(x, y, z, blocksModule, iteration)) processedBlocks.add(pos);
                    }
                }
            }
        }
    }
    //endregion
    //region Helpers
    private float getDistanceToNearestPositionSqr(Coords position)
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
    //endregion
}
