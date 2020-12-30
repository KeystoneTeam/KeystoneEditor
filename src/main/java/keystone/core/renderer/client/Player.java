package keystone.core.renderer.client;

import keystone.core.renderer.client.interop.ClientInterop;
import keystone.core.renderer.client.models.Point;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.RayTraceResult;

public class Player
{
    private static double x;
    private static double y;
    private static double z;
    private static double activeY;
    private static DimensionId dimensionId;
    private static RayTraceResult rayTrace;

    public static void setPosition(double partialTicks, ClientPlayerEntity player)
    {
        x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * partialTicks;
        y = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * partialTicks;
        z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * partialTicks;
        dimensionId = DimensionId.from(player.getEntityWorld().getDimensionKey());
        rayTrace = player.pick(ClientInterop.getRenderDistanceChunks() * 16, (float)partialTicks, true);
    }

    static void setActiveY() {
        activeY = y;
    }

    public static double getX() {
        return x;
    }
    public static double getY() {
        return y;
    }
    public static double getZ() {
        return z;
    }

    public static double getMaxY(double configMaxY) {
        if (configMaxY == -1) {
            return activeY;
        }
        if (configMaxY == 0) {
            return y;
        }
        return configMaxY;
    }

    public static DimensionId getDimensionId() {
        return dimensionId;
    }
    public static RayTraceResult getRayTrace() { return rayTrace; }

    public static Coords getCoords() {
        return new Coords(x, y, z);
    }
    public static Point getPoint() {
        return new Point(x, y, z);
    }
}
