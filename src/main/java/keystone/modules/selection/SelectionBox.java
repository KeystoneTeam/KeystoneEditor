package keystone.modules.selection;

import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.BoundingBoxCuboid;
import keystone.core.renderer.common.models.Coords;

public class SelectionBox extends BoundingBoxCuboid
{
    protected SelectionBox(Coords minCoords, Coords maxCoords)
    {
        super(minCoords, maxCoords, BoundingBoxType.get("selection_box"));
    }
    public static SelectionBox from(Coords minCoords, Coords maxCoords)
    {
        return new SelectionBox(minCoords, maxCoords);
    }
}
