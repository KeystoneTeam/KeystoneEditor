package keystone.core.math;

import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BlockPosMath
{
    public static BlockPos getOrientedBlockPos(BlockPos localPos, Vec3i size, BlockRotation rotation, BlockMirror mirror, int scale)
    {
        int sizeX = size.getX() * scale;
        int sizeZ = size.getZ() * scale;

        BlockPos modifiedPos = localPos;

        BlockRotation check = rotation;
        if (mirror != BlockMirror.NONE)
        {
            if (check == BlockRotation.CLOCKWISE_90) check = BlockRotation.COUNTERCLOCKWISE_90;
            else if (check == BlockRotation.COUNTERCLOCKWISE_90) check = BlockRotation.CLOCKWISE_90;
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

        int xAxisSize = (rotation == BlockRotation.NONE || rotation == BlockRotation.CLOCKWISE_180) ? sizeX : sizeZ;
        int zAxisSize = (rotation == BlockRotation.NONE || rotation == BlockRotation.CLOCKWISE_180) ? sizeZ : sizeX;
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
    public static Vec3d getOrientedVec3d(Vec3d localVector, Vec3i size, BlockRotation rotation, BlockMirror mirror, int scale)
    {
        int sizeX = size.getX() * scale;
        int sizeZ = size.getZ() * scale;

        Vec3d modifiedVector = localVector.multiply(scale, scale, scale);

        BlockRotation check = rotation;
        if (mirror != BlockMirror.NONE)
        {
            if (check == BlockRotation.CLOCKWISE_90) check = BlockRotation.COUNTERCLOCKWISE_90;
            else if (check == BlockRotation.COUNTERCLOCKWISE_90) check = BlockRotation.CLOCKWISE_90;
        }
        switch (check)
        {
            case CLOCKWISE_180:
                modifiedVector = new Vec3d(sizeX - localVector.x, localVector.y, sizeZ - localVector.z);
                break;
            case COUNTERCLOCKWISE_90:
                modifiedVector = new Vec3d(localVector.z, localVector.y, sizeX - localVector.x);
                break;
            case CLOCKWISE_90:
                modifiedVector = new Vec3d(sizeZ - localVector.z, localVector.y, localVector.x);
                break;
        }

        int xAxisSize = (rotation == BlockRotation.NONE || rotation == BlockRotation.CLOCKWISE_180) ? sizeX : sizeZ;
        int zAxisSize = (rotation == BlockRotation.NONE || rotation == BlockRotation.CLOCKWISE_180) ? sizeZ : sizeX;
        switch (mirror)
        {
            case FRONT_BACK:
                modifiedVector = new Vec3d(xAxisSize - modifiedVector.x, modifiedVector.y, modifiedVector.z);
                break;
            case LEFT_RIGHT:
                modifiedVector = new Vec3d(modifiedVector.x, modifiedVector.y, zAxisSize - modifiedVector.z);
                break;
        }

        return modifiedVector;
    }
}
