package keystone.core.math;

import keystone.core.renderer.common.models.Coords;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class RayIntersections
{
    private static Vector3d getPointOnFace(Coords min, Coords max, Direction face)
    {
        switch (face)
        {
            case UP: return new Vector3d(0, max.getY(), 0);
            case DOWN: return new Vector3d(0, min.getY(), 0);
            case NORTH: return new Vector3d(0, 0, min.getZ());
            case SOUTH: return new Vector3d(0, 0, max.getZ());
            case WEST: return new Vector3d(min.getX(), 0, 0);
            case EAST: return new Vector3d(max.getX(), 0, 0);
            default: return null;
        }
    }
    public static Vector3d[] getFacePlane(Coords min, Coords max, Direction face)
    {
        return new Vector3d[] { getPointOnFace(min, max, face), Vector3d.copy(face.getDirectionVec()) };
    }
    public static Vector3d rayPlaneIntersection(Vector3d origin, Vector3d direction, Vector3d pointOnPlane, Vector3d planeNormal)
    {
        double d = pointOnPlane.dotProduct(planeNormal.scale(-1));
        double t = -(d + origin.dotProduct(planeNormal)) / direction.dotProduct(planeNormal);
        return origin.add(direction.scale(t));
    }
    public static Vector3d rayFaceIntersection(Vector3d origin, Vector3d direction, Coords min, Coords max, Direction face)
    {
        Vector3d[] plane = getFacePlane(min, max, face);
        Vector3d intersectionPoint = rayPlaneIntersection(origin, direction, plane[0], plane[1]);
        if (intersectionPoint == null) return null;

        if (face == Direction.UP || face == Direction.DOWN)
        {
            if (intersectionPoint.getX() >= min.getX() && intersectionPoint.getX() <= max.getX() &&
                intersectionPoint.getZ() >= min.getZ() && intersectionPoint.getZ() <= max.getZ()) return intersectionPoint;
            else return null;
        }
        if (face == Direction.NORTH || face == Direction.SOUTH)
        {
            if (intersectionPoint.getX() >= min.getX() && intersectionPoint.getX() <= max.getX() &&
                    intersectionPoint.getY() >= min.getY() && intersectionPoint.getY() <= max.getY()) return intersectionPoint;
            else return null;
        }
        if (face == Direction.EAST || face == Direction.WEST)
        {
            if (intersectionPoint.getZ() >= min.getZ() && intersectionPoint.getZ() <= max.getZ() &&
                    intersectionPoint.getY() >= min.getY() && intersectionPoint.getY() <= max.getY()) return intersectionPoint;
            else return null;
        }

        return null;
    }
}
