package keystone.core.math;

import keystone.core.mixins.client.GameRendererInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vector4f;
import net.minecraft.world.RaycastContext;

public class RayTracing
{
    private static Vec3d getPointOnFace(Vec3i min, Vec3i max, Direction face)
    {
        switch (face)
        {
            case UP: return new Vec3d(0, max.getY(), 0);
            case DOWN: return new Vec3d(0, min.getY(), 0);
            case NORTH: return new Vec3d(0, 0, min.getZ());
            case SOUTH: return new Vec3d(0, 0, max.getZ());
            case WEST: return new Vec3d(min.getX(), 0, 0);
            case EAST: return new Vec3d(max.getX(), 0, 0);
            default: return null;
        }
    }
    public static Vec3d[] getFacePlane(Vec3i min, Vec3i max, Direction face)
    {
        return new Vec3d[] { getPointOnFace(min, max, face), Vec3d.of(face.getVector()) };
    }
    public static Vec3d rayPlaneIntersection(Vec3d origin, Vec3d direction, Vec3d pointOnPlane, Direction planeNormal)
    {
        return rayPlaneIntersection(origin, direction, pointOnPlane, Vec3d.of(planeNormal.getVector()));
    }
    public static Vec3d rayPlaneIntersection(Vec3d origin, Vec3d direction, Vec3i min, Vec3i max, Direction face)
    {
        Vec3d[] plane = getFacePlane(min, max, face);
        return rayPlaneIntersection(origin, direction, plane[0], plane[1]);
    }
    public static Vec3d rayPlaneIntersection(Vec3d origin, Vec3d direction, Vec3d pointOnPlane, Vec3d planeNormal)
    {
        double d = -planeNormal.dotProduct(pointOnPlane);
        double denom = planeNormal.dotProduct(direction);
        if (Math.abs(denom) <= 1e-4f) return null;

        double t = -(planeNormal.dotProduct(origin) + d) / planeNormal.dotProduct(direction);
        if (t <= 0) return null;

        return origin.add(direction.multiply(t));
    }
    public static Vec3d rayFaceIntersection(Vec3d origin, Vec3d direction, Vec3i min, Vec3i max, Direction face)
    {
        Vec3d[] plane = getFacePlane(min, max, face);
        Vec3d intersectionPoint = rayPlaneIntersection(origin, direction, plane[0], plane[1]);
        if (intersectionPoint == null) return null;

        if (face == Direction.UP || face == Direction.DOWN)
        {
            if (intersectionPoint.x >= min.getX() && intersectionPoint.x <= max.getX() &&
                intersectionPoint.z >= min.getZ() && intersectionPoint.z <= max.getZ()) return intersectionPoint;
            else return null;
        }
        if (face == Direction.NORTH || face == Direction.SOUTH)
        {
            if (intersectionPoint.x >= min.getX() && intersectionPoint.x <= max.getX() &&
                    intersectionPoint.y >= min.getY() && intersectionPoint.y <= max.getY()) return intersectionPoint;
            else return null;
        }
        if (face == Direction.EAST || face == Direction.WEST)
        {
            if (intersectionPoint.z >= min.getZ() && intersectionPoint.z <= max.getZ() &&
                    intersectionPoint.y >= min.getY() && intersectionPoint.y <= max.getY()) return intersectionPoint;
            else return null;
        }

        return null;
    }

    public static HitResult rayTraceBlock(Vec3d origin, Vec3d direction, Entity entity, double rayTraceDistance, boolean rayTraceFluids)
    {
        Vec3d vector3d = origin;
        Vec3d vector3d1 = direction;
        Vec3d vector3d2 = vector3d.add(vector3d1.x * rayTraceDistance, vector3d1.y * rayTraceDistance, vector3d1.z * rayTraceDistance);
        return entity.world.raycast(new RaycastContext(vector3d, vector3d2, RaycastContext.ShapeType.OUTLINE, rayTraceFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, entity));
    }

    public static Vec3d screenPointToRayDirection(float partialTicks)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        GameRenderer gr = mc.gameRenderer;

        double x = mc.mouse.getX();
        double y = mc.getWindow().getHeight() - mc.mouse.getY();

        float pitch = mc.getCameraEntity().getPitch(partialTicks);
        float yaw = mc.getCameraEntity().getYaw(partialTicks) + 180.0f;

        MatrixStack stack = new MatrixStack();
        stack.peek().getPositionMatrix().multiply(gr.getBasicProjectionMatrix(((GameRendererInvoker)gr).invokeGetFov(gr.getCamera(), partialTicks, true)));
        stack.multiply(new Quaternion(Vec3f.POSITIVE_X, pitch, true));
        stack.multiply(new Quaternion(Vec3f.POSITIVE_Y, yaw, true));
        Matrix4f matrix = stack.peek().getPositionMatrix();
        if (!matrix.invert()) return Vec3d.ZERO;

        int[] viewport = new int[] { 0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight() };

        // Unproject near and far
        Vec3d near = unproject(new Vec3d(x, y, -1), matrix, viewport);
        Vec3d far = unproject(new Vec3d(x, y, 1), matrix, viewport);

        if (near == null || far == null) return Vec3d.ZERO;

        return new Vec3d(far.x - near.x, far.y - near.y, far.z - near.z).normalize();
    }

    private static Vec3d unproject(Vec3d point, Matrix4f projection, int[] viewport)
    {
        Vector4f unprojected = new Vector4f(
                ((float)point.x - (float)viewport[0]) / (float)viewport[2] * 2.0f - 1.0f,
                ((float)point.y - (float)viewport[1]) / (float)viewport[3] * 2.0f - 1.0f,
                2.0f * (float)point.z - 1.0f, 1.0f);
        unprojected.transform(projection);
        
        if(unprojected.getW() == 0.0f) return null;
        float w = 1.0f / unprojected.getW();

        return new Vec3d(unprojected.getX() * w, unprojected.getY() * w, unprojected.getZ() * w);
    }
}
