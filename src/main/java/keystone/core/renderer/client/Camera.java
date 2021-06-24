package keystone.core.renderer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

public class Camera
{
    public static Vector3d getPosition()
    {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    }
    public static Quaternion getRotation()
    {
        return Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
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
}
