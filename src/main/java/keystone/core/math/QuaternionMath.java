package keystone.core.math;


import net.minecraft.util.math.Quaternion;

public class QuaternionMath
{
    public static Quaternion euler(double yaw, double pitch, double roll)
    {
        double cy = Math.cos(Math.toRadians(yaw * 0.5));
        double sy = Math.sin(Math.toRadians(yaw * 0.5));
        double cp = Math.cos(Math.toRadians(pitch * 0.5));
        double sp = Math.sin(Math.toRadians(pitch * 0.5));
        double cr = Math.cos(Math.toRadians(roll * 0.5));
        double sr = Math.sin(Math.toRadians(roll * 0.5));

        return new Quaternion(
                (float)(cr * cp * cy + sr * sp * sy),
                (float)(sr * cp * cy - cr * sp * sy),
                (float)(cr * sp * cy + sr * cp * sy),
                (float)(cr * cp * sy - sr * sp * cy)
        );
    }
}
