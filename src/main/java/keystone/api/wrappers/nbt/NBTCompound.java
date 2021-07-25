package keystone.api.wrappers.nbt;

import net.minecraft.nbt.CompoundNBT;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NBTCompound extends NBTWrapper<CompoundNBT>
{
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param nbt The Minecraft INBT
     */
    public NBTCompound(CompoundNBT nbt) { super(nbt); }
    public NBTCompound() { super(new CompoundNBT()); }

    public Set<String> getAllKeys() { return nbt.getAllKeys(); }

    public boolean contains(String key) { return this.nbt.contains(key); }
    public boolean contains(String key, NBTType tagType) { return this.nbt.contains(key, tagType.minecraftID); }
    public void remove(String key) { this.nbt.remove(key); }

    public void put(String key, NBTWrapper<?> nbt) { this.nbt.put(key, nbt.getMinecraftNBT()); }
    public void putByte(String key, byte value) { nbt.putByte(key, value); }
    public void putShort(String key, short value) { nbt.putShort(key, value); }
    public void putInt(String key, int value) { nbt.putInt(key, value); }
    public void putLong(String key, long value) { nbt.putLong(key, value); }
    public void putUUID(String key, UUID value) { nbt.putUUID(key, value); }
    public void putFloat(String key, float value) { nbt.putFloat(key, value); }
    public void putDouble(String key, double value) { nbt.putDouble(key, value); }
    public void putString(String key, String value) { nbt.putString(key, value); }
    public void putByteArray(String key, byte[] value) { nbt.putByteArray(key, value); }
    public void putIntArray(String key, int[] value) { nbt.putIntArray(key, value); }
    public void putIntArray(String key, List<Integer> value) { nbt.putIntArray(key, value); }
    public void putLongArray(String key, long[] value) { nbt.putLongArray(key, value); }
    public void putLongArray(String key, List<Long> value) { nbt.putLongArray(key, value); }
    public void putBoolean(String key, boolean value) { nbt.putBoolean(key, value); }

    public NBTCompound getCompound(String key) { return new NBTCompound(nbt.getCompound(key)); }
    public NBTList getList(String key, NBTType nbtType) { return new NBTList(nbt.getList(key, nbtType.minecraftID)); }
    public byte getByte(String key) { return nbt.getByte(key); }
    public short getShort(String key) { return nbt.getShort(key); }
    public int getInt(String key) { return nbt.getInt(key); }
    public long getLong(String key) { return nbt.getLong(key); }
    public UUID getUUID(String key) { return nbt.getUUID(key); }
    public float getFloat(String key) { return nbt.getFloat(key); }
    public double getDouble(String key) { return nbt.getDouble(key); }
    public String getString(String key) { return nbt.getString(key); }
    public byte[] getByteArray(String key) { return nbt.getByteArray(key); }
    public int[] getIntArray(String key) { return nbt.getIntArray(key); }
    public long[] getLongArray(String key) { return nbt.getLongArray(key); }
    public boolean getBoolean(String key) { return nbt.getBoolean(key); }
}
