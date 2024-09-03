package keystone.api.utils;

import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PositionSampler
{
    public static List<Vector2f> sample2D(float radius, int minX, int minZ, int regionWidth, int regionDepth) { return sample2D(Keystone.RANDOM, radius, minX, minZ, regionWidth, regionDepth, 30); }
    public static List<Vector2f> sample2D(Random random, float radius, int minX, int minZ, int regionWidth, int regionDepth) { return sample2D(random, radius, minX, minZ, regionWidth, regionDepth, 30); }
    public static List<Vector2f> sample2D(Random random, float radius, int minX, int minZ, int regionWidth, int regionDepth, int maxAttemptsPerSample)
    {
        List<Vector2f> points = new ArrayList<>();
        List<Vector2f> activeList = new ArrayList<>();
        float radiusSqr = radius * radius;
        
        // Grid size relative to the radius
        float cellSize = radius / (float) Math.sqrt(2);
        int gridWidth = (int) Math.ceil(regionWidth / cellSize);
        int gridHeight = (int) Math.ceil(regionDepth / cellSize);
        
        // Initialize grid to store points
        Vector2f[][] grid = new Vector2f[gridWidth][gridHeight];
        
        // Helper function to check if a point is valid
        
        // Add initial point
        float startX = minX + random.nextFloat() * regionWidth;
        float startZ = minZ + random.nextFloat() * regionDepth;
        Vector2f initialPoint = new Vector2f(startX, startZ);
        points.add(initialPoint);
        activeList.add(initialPoint);
        grid[(int) ((startX - minX) / cellSize)][(int) ((startZ - minZ) / cellSize)] = initialPoint;
        
        while (!activeList.isEmpty())
        {
            int index = random.nextInt(activeList.size());
            Vector2f point = activeList.get(index);
            boolean found = false;
            
            // Try to generate a new point around the active point
            for (int i = 0; i < maxAttemptsPerSample; i++)
            {
                double angle = random.nextDouble() * Math.PI * 2;
                float newRadius = radius + random.nextFloat() * radius;
                float newX = point.x + (float) (Math.cos(angle) * newRadius);
                float newZ = point.y + (float) (Math.sin(angle) * newRadius);
                Vector2f newPoint = new Vector2f(newX, newZ);
                
                // Check if the new point is within bounds and valid
                if (newX >= minX && newX < minX + regionWidth &&
                        newZ >= minZ && newZ < minZ + regionDepth &&
                        isValid(newPoint, radiusSqr, minX, minZ, cellSize, gridWidth, gridHeight, grid))
                {
                    points.add(newPoint);
                    activeList.add(newPoint);
                    grid[(int) ((newX - minX) / cellSize)][(int) ((newZ - minZ) / cellSize)] = newPoint;
                    found = true;
                    break;
                }
            }
            
            // If no valid point is found, remove the point from the active list
            if (!found) {
                activeList.remove(index);
            }
        }
        
        return points;
    }
    
    private static boolean isValid(Vector2f point, float radiusSqr, int minX, int minZ, float cellSize, int gridWidth, int gridHeight, Vector2f[][] grid)
    {
        int gridX = (int)((point.x - minX) / cellSize);
        int gridZ = (int)((point.y - minZ) / cellSize);
        
        for (int i = Math.max(0, gridX - 2); i <= Math.min(gridWidth - 1, gridX + 2); i++)
        {
            for (int j = Math.max(0, gridZ - 2); j <= Math.min(gridHeight - 1, gridZ + 2); j++)
            {
                Vector2f neighbor = grid[i][j];
                if (neighbor != null && distanceSqr(point, neighbor) < radiusSqr) return false;
            }
        }
        return true;
}
    
    private static float distanceSqr(Vector2f p1, Vector2f p2)
    {
        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;
        return dx * dx + dy * dy;
    }
}
