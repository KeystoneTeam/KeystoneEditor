package keystone.core.renderer.client.renderers;

import keystone.core.renderer.client.models.Point;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class SphereMesh
{
    private List<Point> verticesList;
    private List<Integer> triangles;
    private int triangleOffset;
    private Point[] points;

    public SphereMesh(int resolution)
    {
        verticesList = new ArrayList<>();
        triangles = new ArrayList<>();
        triangleOffset = 0;

        for (Direction direction : Direction.values()) generateFace(direction, resolution);
        points = new Point[triangles.size()];
        for (int i = 0; i < points.length; i++) points[i] = verticesList.get(triangles.get(i));
    }
    public Point[] getPoints()
    {
        return points;
    }

    private void generateFace(Direction direction, int resolution)
    {
        Vector3d localUp = Vector3d.atLowerCornerOf(direction.getNormal());
        Vector3d axisA = new Vector3d(localUp.y, localUp.z, localUp.x);
        Vector3d axisB = localUp.cross(axisA);

        Point[] points = new Point[resolution * resolution];
        int[] tris = new int[(resolution - 1) * (resolution - 1) * 6];
        int triIndex = 0;

        for (int y = 0; y < resolution; y++)
        {
            for (int x = 0; x < resolution; x++)
            {
                int i = x + y * resolution;
                Vector2f percent = new Vector2f((float)x / (resolution - 1), (float)y/ (resolution - 1));

                double cx = localUp.x + (percent.x - .5) * 2 * axisA.x + (percent.y - .5) * 2 * axisB.x;
                double cy = localUp.y + (percent.x - .5) * 2 * axisA.y + (percent.y - .5) * 2 * axisB.y;
                double cz = localUp.z + (percent.x - .5) * 2 * axisA.z + (percent.y - .5) * 2 * axisB.z;
                Vector3d pointOnUnitCube = new Vector3d(cx, cy, cz);
                Vector3d pointOnUnitSphere = pointOnUnitCube.normalize();

                points[i] = new Point(pointOnUnitSphere);

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

        for (Point point : points) verticesList.add(point);
        for (int tri : tris) triangles.add(tri + triangleOffset);
        triangleOffset = verticesList.size();
    }
}
