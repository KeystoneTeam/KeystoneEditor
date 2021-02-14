package keystone.core.modules.brush.boxes;

import keystone.core.modules.brush.BrushModule;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.MathHelper;
import keystone.core.renderer.common.models.AbstractBoundingBox;

public class BrushPreviewBox extends AbstractBoundingBox
{
    private BrushModule brushModule;

    public BrushPreviewBox(BrushModule brushModule)
    {
        super(BoundingBoxType.get("brush_preview"));
        this.brushModule = brushModule;
    }

    public BrushModule getBrushModule()
    {
        return brushModule;
    }

    @Override
    public Boolean intersectsBounds(int minX, int minZ, int maxX, int maxZ)
    {
        int[] brushSize = brushModule.getBrushSize();
        int x = Player.getHighlightedBlock().getX() - (brushSize[0] / 2);
        int z = Player.getHighlightedBlock().getZ() - (brushSize[2] / 2);
        return minX <= x && maxX >= x + brushSize[0] && minZ <= z && maxZ >= brushSize[2];
    }

    @Override
    public double getDistanceX(double x)
    {
        int[] brushSize = brushModule.getBrushSize();
        int minX = Player.getHighlightedBlock().getX() - (brushSize[0] / 2);
        return x - MathHelper.clamp(x, minX, minX + brushSize[0]);
    }
    @Override
    public double getDistanceY(double y)
    {
        int[] brushSize = brushModule.getBrushSize();
        int minY = Player.getHighlightedBlock().getY() - (brushSize[1] / 2);
        return y - MathHelper.clamp(y, minY, minY + brushSize[1]);
    }
    @Override
    public double getDistanceZ(double z)
    {
        int[] brushSize = brushModule.getBrushSize();
        int minZ = Player.getHighlightedBlock().getZ() - (brushSize[2] / 2);
        return z - MathHelper.clamp(z, minZ, minZ + brushSize[2]);
    }
}
