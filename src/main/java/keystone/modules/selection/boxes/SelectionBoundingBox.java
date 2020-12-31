package keystone.modules.selection.boxes;

import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.BoundingBoxCuboid;
import keystone.core.renderer.common.models.Coords;

public class SelectionBoundingBox extends BoundingBoxCuboid
{
    protected SelectionBoundingBox(Coords minCoords, Coords maxCoords)
    {
        super(minCoords, maxCoords, BoundingBoxType.get("selection_box"));
    }
    public static SelectionBoundingBox startNew(Coords coords)
    {
        return new SelectionBoundingBox(coords, coords);
    }
}
