package keystone.api;

import keystone.api.wrappers.coordinates.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiscSampler
{
    public static final Random RANDOM = new Random();

    private static int ceil(double a) { return (int)Math.ceil(a); }

    public static List<Vector2f> sample2D(float radius, int regionWidth, int regionHeight) { return sample2D(RANDOM, radius, regionWidth, regionHeight, 30); }
    public static List<Vector2f> sample2D(Random random, float radius, int regionWidth, int regionHeight) { return sample2D(random, radius, regionWidth, regionHeight, 30); }
    public static List<Vector2f> sample2D(Random random, float radius, int regionWidth, int regionHeight, int samplesBeforeRejection)
    {
        float cellSize = radius / (float)Math.sqrt(2);
        int[][] grid = new int[ceil(regionWidth / cellSize)][];
        for (int i = 0; i < grid.length; i++) grid[i] = new int[ceil(regionHeight / cellSize)];
        List<Vector2f> points = new ArrayList<>();
        List<Vector2f> spawnPoints = new ArrayList<>();

        spawnPoints.add(new Vector2f(regionWidth * 0.5f, regionHeight * 0.5f));
        while (spawnPoints.size() > 0)
        {
            int spawnIndex = random.nextInt(spawnPoints.size());
            Vector2f spawnCenter = spawnPoints.get(spawnIndex);
            boolean accepted = false;

            for (int i = 0; i < samplesBeforeRejection; i++)
            {
                double angle = random.nextFloat() * 2 * Math.PI;
                double dx = Math.sin(angle);
                double dz = Math.cos(angle);
                double distance = radius + random.nextDouble() * radius;
                Vector2f candidate = new Vector2f((float)(spawnCenter.x + dx * distance), (float)(spawnCenter.y + dz * distance));
                if (valid2D(candidate, regionWidth, regionHeight, cellSize, radius, points, grid))
                {
                    points.add(candidate);
                    spawnPoints.add(candidate);
                    grid[(int)(candidate.x / cellSize)][(int)(candidate.y / cellSize)] = points.size();
                    accepted = true;
                    break;
                }
            }

            if (!accepted) spawnPoints.remove(spawnIndex);
        }

        return points;
    }

    private static boolean valid2D(Vector2f candidate, int regionWidth, int regionHeight, float cellSize, float radius, List<Vector2f> points, int[][] grid)
    {
        if (candidate.x >= 0 && candidate.x < regionWidth && candidate.y >= 0 && candidate.y < regionHeight)
        {
            int cellX = (int)(candidate.x / cellSize);
            int cellY = (int)(candidate.y / cellSize);
            int searchStartX = Math.max(0, cellX - 2);
            int searchEndX = Math.min(cellX + 2, grid.length - 1);
            int searchStartY = Math.max(0, cellY - 2);
            int searchEndY = Math.min(cellY + 2, grid[0].length - 1);

            for (int x = searchStartX; x <= searchEndX; x++)
            {
                for (int y = searchStartY; y <= searchEndY; y++)
                {
                    int pointIndex = grid[x][y] - 1;
                    if (pointIndex != -1)
                    {
                        float dx = candidate.x - points.get(pointIndex).x;
                        float dy = candidate.y - points.get(pointIndex).y;
                        float distanceSqr = dx * dx + dy * dy;
                        if (distanceSqr < radius * radius) return false;
                    }
                }
            }

            return true;
        }
        return false;
    }
}
