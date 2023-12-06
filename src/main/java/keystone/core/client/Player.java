package keystone.core.client;

import keystone.core.KeystoneGlobalState;
import keystone.core.math.RayTracing;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Player
{
    private static ClientPlayerEntity player;
    private static double x;
    private static double y;
    private static double z;
    private static double pitch;
    private static double yaw;
    private static Vec3d eyePosition;
    private static Vec3d lookDirection;
    private static RegistryKey<World> dimension;
    private static HitResult rayTrace;

    public static void update(double partialTicks, ClientPlayerEntity player)
    {
        Player.player = player;

        x = player.prevX + (player.getX() - player.prevX) * partialTicks;
        y = player.prevY + (player.getY() - player.prevY) * partialTicks;
        z = player.prevZ + (player.getZ() - player.prevZ) * partialTicks;
        pitch = player.getPitch((float)partialTicks);
        yaw = player.getYaw((float)partialTicks) % 360;
        eyePosition = player.getEyePos();
        lookDirection = KeystoneGlobalState.CloseSelection ? player.getRotationVec((float)partialTicks) : RayTracing.screenPointToRayDirection((float)partialTicks);

        dimension = player.getEntityWorld().getRegistryKey();
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
    public static Vec3d getEyePosition() { return eyePosition; }
    public static Vec3d getLookDirection() { return lookDirection; }

    public static RegistryKey<World> getDimension()
    {
        return dimension;
    }

    public static BlockPos getHighlightedBlock()
    {
        if (rayTrace == null || rayTrace.getType() != HitResult.Type.BLOCK) return BlockPos.ofFloored(getEyePosition().add(lookDirection.multiply(KeystoneGlobalState.CloseSelectionDistance)));
        else
        {
            BlockHitResult blockRay = (BlockHitResult)rayTrace;
            return blockRay.getBlockPos();
        }
    }
    public static void updateHighlightedBlock()
    {
        rayTrace = KeystoneGlobalState.CloseSelection ? null : RayTracing.rayTraceBlock(eyePosition, lookDirection, player, Camera.getRenderDistanceBlocks(), true);
    }
}
