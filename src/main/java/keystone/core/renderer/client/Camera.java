package keystone.core.renderer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class Camera
{
    private static Vector3d getPos()
    {
        return Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
    }

    public static double getX()
    {
        return getPos().x;
    }

    public static double getY()
    {
        return getPos().y;
    }

    public static double getZ()
    {
        return getPos().z;
    }
}
