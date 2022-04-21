package keystone.core.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;

public final class EntityUtils
{
    public static NbtCompound getEntityData(Entity entity)
    {
        NbtCompound nbt = new NbtCompound();
        nbt.putString(Entity.ID_KEY, EntityType.getId(entity.getType()).toString());
        entity.writeNbt(nbt);
        return nbt;
    }
    public static NbtCompound getEntityDataNoUuid(Entity entity)
    {
        NbtCompound nbt = new NbtCompound();
        nbt.putString(Entity.ID_KEY, EntityType.getId(entity.getType()).toString());
        entity.writeNbt(nbt);
        nbt.remove(Entity.UUID_KEY);
        return nbt;
    }
}
