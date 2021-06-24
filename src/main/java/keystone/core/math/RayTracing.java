package keystone.core.math;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import keystone.api.Keystone;
import keystone.core.renderer.client.Camera;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;

import java.nio.FloatBuffer;

public class RayTracing
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
        return new Vector3d[] { getPointOnFace(min, max, face), Vector3d.atLowerCornerOf(face.getNormal()) };
    }
    public static Vector3d rayPlaneIntersection(Vector3d origin, Vector3d direction, Vector3d pointOnPlane, Direction planeNormal)
    {
        return rayPlaneIntersection(origin, direction, pointOnPlane, Vector3d.atLowerCornerOf(planeNormal.getNormal()));
    }
    public static Vector3d rayPlaneIntersection(Vector3d origin, Vector3d direction, Coords min, Coords max, Direction face)
    {
        Vector3d[] plane = getFacePlane(min, max, face);
        return rayPlaneIntersection(origin, direction, plane[0], plane[1]);
    }
    public static Vector3d rayPlaneIntersection(Vector3d origin, Vector3d direction, Vector3d pointOnPlane, Vector3d planeNormal)
    {
        double d = -planeNormal.dot(pointOnPlane);
        double denom = planeNormal.dot(direction);
        if (Math.abs(denom) <= 1e-4f) return null;

        double t = -(planeNormal.dot(origin) + d) / planeNormal.dot(direction);
        if (t <= 0) return null;

        return origin.add(direction.scale(t));
    }
    public static Vector3d rayFaceIntersection(Vector3d origin, Vector3d direction, Coords min, Coords max, Direction face)
    {
        Vector3d[] plane = getFacePlane(min, max, face);
        Vector3d intersectionPoint = rayPlaneIntersection(origin, direction, plane[0], plane[1]);
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

    public static RayTraceResult rayTraceBlock(Vector3d origin, Vector3d direction, Entity entity, double rayTraceDistance, boolean rayTraceFluids)
    {
        Vector3d vector3d = origin;
        Vector3d vector3d1 = direction;
        Vector3d vector3d2 = vector3d.add(vector3d1.x * rayTraceDistance, vector3d1.y * rayTraceDistance, vector3d1.z * rayTraceDistance);
        return entity.level.clip(new RayTraceContext(vector3d, vector3d2, RayTraceContext.BlockMode.OUTLINE, rayTraceFluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, entity));
    }

    public static Vector3d screenPointToRayDirection(float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gr = mc.gameRenderer;

        double x = mc.mouseHandler.xpos();
        double y = mc.getWindow().getHeight() - mc.mouseHandler.ypos();

        float pitch = mc.getCameraEntity().getViewXRot(partialTicks);
        float yaw = mc.getCameraEntity().getViewYRot(partialTicks) + 180.0f;

        MatrixStack stack = new MatrixStack();
        stack.last().pose().multiply(gr.getProjectionMatrix(gr.getMainCamera(), partialTicks, true));
        stack.mulPose(new Quaternion(Vector3f.XP, pitch, true));
        stack.mulPose(new Quaternion(Vector3f.YP, yaw, true));
        Matrix4f matrix = stack.last().pose();
        if (!matrix.invert()) return Vector3d.ZERO;

        int[] viewport = new int[] { 0, 0, mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight() };

        // Unproject near and far
        Vector3d near = unproject(new Vector3d(x, y, -1), matrix, viewport);
        Vector3d far = unproject(new Vector3d(x, y, 1), matrix, viewport);

        if (near == null || far == null) return Vector3d.ZERO;

        return new Vector3d(far.x - near.x, far.y - near.y, far.z - near.z).normalize();
    }

    private static Vector3d unproject(Vector3d point, Matrix4f projection, int[] viewport)
    {
        Vector4f unprojected = new Vector4f(
                ((float)point.x - (float)viewport[0]) / (float)viewport[2] * 2.0f - 1.0f,
                ((float)point.y - (float)viewport[1]) / (float)viewport[3] * 2.0f - 1.0f,
                2.0f * (float)point.z - 1.0f, 1.0f);
        unprojected.transform(projection);
        
        if(unprojected.w() == 0.0f) return null;
        float w = 1.0f / unprojected.w();

        return new Vector3d(unprojected.x() * w, unprojected.y() * w, unprojected.z() * w);
    }
}
