package keystone.core.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public class Camera
{
    public static Vec3d getPosition()
    {
        return MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
    }
    public static Quaternionf getRotation()
    {
        return MinecraftClient.getInstance().gameRenderer.getCamera().getRotation();
    }

    public static double getX()
    {
        return getPosition().x;
    }

    public static double getY()
    {
        return getPosition().y;
    }

    public static double getZ()
    {
        return getPosition().z;
    }

    public static int getRenderDistanceChunks()
    {
        return MinecraftClient.getInstance().options.getViewDistance().getValue();
    }

    public static int getRenderDistanceBlocks()
    {
        return getRenderDistanceChunks() << 4;
    }
}
