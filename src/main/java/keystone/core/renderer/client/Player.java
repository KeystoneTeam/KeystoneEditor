package keystone.core.renderer.client;

import keystone.core.KeystoneGlobalState;
import keystone.core.math.RayTracing;
import keystone.core.renderer.client.interop.ClientInterop;
import keystone.core.renderer.client.models.Point;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class Player
{
    private static ClientPlayerEntity player;
    private static double x;
    private static double y;
    private static double z;
    private static double pitch;
    private static double yaw;
    private static Vector3d eyePosition;
    private static Vector3d lookDirection;
    private static DimensionId dimensionId;
    private static RayTraceResult rayTrace;

    public static void update(double partialTicks, ClientPlayerEntity player)
    {
        Player.player = player;

        x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * partialTicks;
        y = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * partialTicks;
        z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * partialTicks;
        pitch = player.getPitch((float)partialTicks);
        yaw = player.getYaw((float)partialTicks) % 360;
        eyePosition = player.getEyePosition((float)partialTicks);
        lookDirection = KeystoneGlobalState.CloseSelection ? player.getLook((float)partialTicks) : RayTracing.screenPointToRayDirection((float)partialTicks);

        dimensionId = DimensionId.from(player.getEntityWorld().getDimensionKey());
        updateHighlightedBlock();
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
    public static double getPitch() { return pitch; }
    public static double getYaw() { return yaw; }
    public static Vector3d getEyePosition() { return eyePosition; }
    public static Vector3d getLookDirection() { return lookDirection; }

    public static DimensionId getDimensionId() {
        return dimensionId;
    }

    public static Coords getCoords() {
        return new Coords(x, y, z);
    }
    public static Point getPoint() {
        return new Point(x, y, z);
    }

    public static Coords getHighlightedBlock()
    {
        if (rayTrace == null || rayTrace.getType() != RayTraceResult.Type.BLOCK) return new Coords(getEyePosition().add(lookDirection.scale(KeystoneGlobalState.CloseSelectionDistance)));
        else
        {
            BlockRayTraceResult blockRay = (BlockRayTraceResult)rayTrace;
            return new Coords(blockRay.getPos());
        }
    }
    public static void updateHighlightedBlock()
    {
        rayTrace = KeystoneGlobalState.CloseSelection ? null : RayTracing.rayTraceBlock(eyePosition, lookDirection, player, ClientInterop.getRenderDistanceChunks() * 16, true);
    }
}
