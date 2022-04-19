package keystone.core.renderer.shapes;

import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SphereMesh
{
    private List<Vec3d> verticesList;
    private List<Integer> triangles;
    private int triangleOffset;
    private Vec3d[] points;

    public SphereMesh(int resolution)
    {
        verticesList = new ArrayList<>();
        triangles = new ArrayList<>();
        triangleOffset = 0;

        for (Direction direction : Direction.values()) generateFace(direction, resolution);
        points = new Vec3d[triangles.size()];
        for (int i = 0; i < points.length; i++) points[i] = verticesList.get(triangles.get(i));
    }
    public Vec3d[] getVec3ds()
    {
        return points;
    }

    private void generateFace(Direction direction, int resolution)
    {
        Vec3d localUp = Vec3d.of(direction.getVector());
        Vec3d axisA = new Vec3d(localUp.y, localUp.z, localUp.x);
        Vec3d axisB = localUp.crossProduct(axisA);

        Vec3d[] points = new Vec3d[resolution * resolution];
        int[] tris = new int[(resolution - 1) * (resolution - 1) * 6];
        int triIndex = 0;

        for (int y = 0; y < resolution; y++)
        {
            for (int x = 0; x < resolution; x++)
            {
                int i = x + y * resolution;
                Vector2f percent = new Vector2f((float)x / (resolution - 1), (float)y/ (resolution - 1));

                double cx = localUp.x + (percent.getX() - .5) * 2 * axisA.x + (percent.getY() - .5) * 2 * axisB.x;
                double cy = localUp.y + (percent.getX() - .5) * 2 * axisA.y + (percent.getY() - .5) * 2 * axisB.y;
                double cz = localUp.z + (percent.getX() - .5) * 2 * axisA.z + (percent.getY() - .5) * 2 * axisB.z;
                Vec3d pointOnUnitCube = new Vec3d(cx, cy, cz);
                points[i] = pointOnUnitCube.normalize();

                if (x != resolution - 1 && y != resolution - 1)
                {
                    tris[triIndex] = i;
                    tris[triIndex + 1] = i + resolution + 1;
                    tris[triIndex + 2] = i + resolution;

                    tris[triIndex + 3] = i;
                    tris[triIndex + 4] = i + 1;
                    tris[triIndex + 5] = i + resolution + 1;
                    triIndex += 6;
                }
            }
        }

        for (Vec3d point : points) verticesList.add(point);
        for (int tri : tris) triangles.add(tri + triangleOffset);
        triangleOffset = verticesList.size();
    }
}
