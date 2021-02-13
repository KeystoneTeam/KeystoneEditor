package keystone.core.modules.brush;

import keystone.api.Keystone;
import keystone.api.SelectionBox;
import keystone.api.wrappers.Block;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BrushModule implements IKeystoneModule
{
    private float minDistanceSqr;
    private BrushShape brushShape;
    private BrushOperation brushOperation;
    private int[] brushSize;
    private List<Coords> brushPositions = new ArrayList<>();

    public BrushModule()
    {
        MinecraftForge.EVENT_BUS.register(this);
        setMinDistance(1);
        setBrushShape(BrushShape.ROUND);
        setBrushOperation(BrushOperation.FILL);
        setBrushSize(5, 5, 5);
    }

    @Override
    public boolean isEnabled() { return KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.BRUSH; }

    //region Input Handlers
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
        }
    }
    @SubscribeEvent
    public final void onMouseDrag(final KeystoneInputEvent.MouseDragEvent event)
    {
        if (!Keystone.isActive() || !isEnabled() || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) addBrushPosition(Player.getHighlightedBlock());
    }
    @SubscribeEvent
    public final void onMouseDragEnd(final KeystoneInputEvent.MouseDragEndEvent event)
    {
        if (!Keystone.isActive() || !isEnabled() || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            addBrushPosition(Player.getHighlightedBlock());
            executeBrush();
        }
    }
    //endregion
    //region Brush Functions
    public void setMinDistance(int minDistance)
    {
        this.minDistanceSqr = minDistance * minDistance;
    }
    public void setBrushShape(BrushShape shape)
    {
        this.brushShape = shape;
    }
    public void setBrushOperation(BrushOperation operation)
    {
        this.brushOperation = operation;
    }
    public void setBrushSize(int x, int y, int z)
    {
        if (x <= 0) x = 1;
        if (y <= 0) y = 1;
        if (z <= 0) z = 1;
        brushSize = new int[] { x, y, z };
    }

    public void prepareBrush()
    {
        brushPositions.clear();
    }

    public void addBrushPosition(Coords position) { addBrushPosition(position, false); }
    public void addBrushPosition(Coords position, boolean force)
    {
        if (position == null) return;

        if (force) brushPositions.add(position);
        else
        {
            float dist = -1;
            for (Coords test : brushPositions)
            {
                float testDist = position.distanceSqr(test);
                if (dist < 0 || testDist < dist) dist = testDist;
            }
            if (dist >= minDistanceSqr || dist < 0) brushPositions.add(position);
        }
    }

    public void executeBrush()
    {
        Keystone.runOnMainThread(() ->
        {
            World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimensionId());
            if (world == null)
            {
                brushPositions.clear();
                return;
            }
            else brushOperation.prepare(world);

            boolean[] shapeMask = this.brushShape.getShapeMask(brushSize[0], brushSize[1], brushSize[2]);
            List<BlockPos> processedBlocks = new ArrayList<>();
            for (Coords position : brushPositions)
            {
                Coords min = position.sub(brushSize[0] / 2, brushSize[1] / 2, brushSize[2] / 2);
                Coords max = min.add(brushSize[0] - 1, brushSize[1] - 1, brushSize[2] - 1);
                SelectionBox box = new SelectionBox(min, max, world);
                executeBrush(box, processedBlocks, shapeMask);

                box.forEachBlock(pos ->
                {
                    world.setBlockState(pos, box.getBlock(pos, false));
                });
            }
        });
    }
    private void executeBrush(SelectionBox box, List<BlockPos> processedBlocks, boolean[] shapeMask)
    {
        box.forEachBlock(pos ->
        {
            BlockPos nPos = pos.subtract(box.getMin());

            int maskIndex = nPos.getX() + nPos.getY() * brushSize[0] + nPos.getZ() * brushSize[0] * brushSize[1];
            if (!processedBlocks.contains(pos) && shapeMask[maskIndex])
            {
                Block brushResult = brushOperation.process(pos);
                if (brushResult != null) box.setBlock(pos, brushResult.getMinecraftBlock());
                processedBlocks.add(pos);
            }
        });
    }
    //endregion
}
