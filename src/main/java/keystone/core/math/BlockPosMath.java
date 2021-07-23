package keystone.core.math;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class BlockPosMath
{
    public static BlockPos getOrientedBlockPos(BlockPos localPos, Vector3i size, Rotation rotation, Mirror mirror, int scale)
    {
        int sizeX = size.getX() * scale;
        int sizeZ = size.getZ() * scale;

        BlockPos modifiedPos = localPos;

        Rotation check = rotation;
        if (mirror != Mirror.NONE)
        {
            if (check == Rotation.CLOCKWISE_90) check = Rotation.COUNTERCLOCKWISE_90;
            else if (check == Rotation.COUNTERCLOCKWISE_90) check = Rotation.CLOCKWISE_90;
        }
        switch (check)
        {
            case CLOCKWISE_180:
                modifiedPos = new BlockPos(sizeX - localPos.getX() - 1, localPos.getY(), sizeZ - localPos.getZ() - 1);
                break;
            case COUNTERCLOCKWISE_90:
                modifiedPos = new BlockPos(localPos.getZ(), localPos.getY(), sizeX - localPos.getX() - 1);
                break;
            case CLOCKWISE_90:
                modifiedPos = new BlockPos(sizeZ - localPos.getZ() - 1, localPos.getY(), localPos.getX());
                break;
        }

        int xAxisSize = (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) ? sizeX : sizeZ;
        int zAxisSize = (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) ? sizeZ : sizeX;
        switch (mirror)
        {
            case FRONT_BACK:
                modifiedPos = new BlockPos(xAxisSize - modifiedPos.getX() - 1, modifiedPos.getY(), modifiedPos.getZ());
                break;
            case LEFT_RIGHT:
                modifiedPos = new BlockPos(modifiedPos.getX(), modifiedPos.getY(), zAxisSize - modifiedPos.getZ() - 1);
                break;
        }

        return modifiedPos;
    }
    public static Vector3d getOrientedVector3d(Vector3d localVector, Vector3i size, Rotation rotation, Mirror mirror, int scale)
    {
        int sizeX = size.getX() * scale;
        int sizeZ = size.getZ() * scale;

        Vector3d modifiedVector = localVector;

        Rotation check = rotation;
        if (mirror != Mirror.NONE)
        {
            if (check == Rotation.CLOCKWISE_90) check = Rotation.COUNTERCLOCKWISE_90;
            else if (check == Rotation.COUNTERCLOCKWISE_90) check = Rotation.CLOCKWISE_90;
        }
        switch (check)
        {
            case CLOCKWISE_180:
                modifiedVector = new Vector3d(sizeX - localVector.x, localVector.y, sizeZ - localVector.z);
                break;
            case COUNTERCLOCKWISE_90:
                modifiedVector = new Vector3d(localVector.z, localVector.y, sizeX - localVector.x);
                break;
            case CLOCKWISE_90:
                modifiedVector = new Vector3d(sizeZ - localVector.z, localVector.y, localVector.x);
                break;
        }

        int xAxisSize = (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) ? sizeX : sizeZ;
        int zAxisSize = (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) ? sizeZ : sizeX;
        switch (mirror)
        {
            case FRONT_BACK:
                modifiedVector = new Vector3d(xAxisSize - modifiedVector.x, modifiedVector.y, modifiedVector.z);
                break;
            case LEFT_RIGHT:
                modifiedVector = new Vector3d(modifiedVector.x, modifiedVector.y, zAxisSize - modifiedVector.z);
                break;
        }

        return modifiedVector;
    }
}
