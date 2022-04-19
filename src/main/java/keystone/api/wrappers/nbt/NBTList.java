package keystone.api.wrappers.nbt;

import net.minecraft.nbt.*;

import java.util.List;

public class NBTList<T> extends NBTWrapper<NbtList>
{
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param nbt The Minecraft NbtElement
     */
    public NBTList(NbtList nbt) { super(nbt); }
    public NBTList() { super(new NbtList()); }

    public NBTType listType() { return NBTType.fromMinecraftID(nbt.getType()); }
    public int size() { return nbt.size(); }
    public void remove(int i) { this.nbt.remove(i); }

    //region Retrieval
    public NBTCompound getCompound(int i) { return new NBTCompound(nbt.getCompound(i)); }
    public <T2> NBTList<T2> getList(int i) { return new NBTList<>(nbt.getList(i)); }
    public short getShort(int i) { return nbt.getShort(i); }
    public int getInt(int i) { return nbt.getInt(i); }
    public int[] getIntArray(int i) { return nbt.getIntArray(i); }
    public double getDouble(int i) { return nbt.getDouble(i); }
    public float getFloat(int i) { return nbt.getFloat(i); }
    public String getString(int i) { return nbt.getString(i); }
    //endregion
    //region Indexed Addition
    public boolean add(int i, NBTWrapper<?> nbt)
    {
        if (this.nbt.getType() == nbt.getMinecraftNBT().getType() || this.nbt.getType() == 0)
        {
            this.nbt.add(i, nbt.getMinecraftNBT());
            return true;
        }
        else return false;
    }
    public boolean add(int i, short value)
    {
        if (this.nbt.getType() == NbtElement.SHORT_TYPE || this.nbt.getType() == 0)
        {
            this.nbt.add(i, NbtShort.of(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, int value)
    {
        if (this.nbt.getType() == NbtElement.INT_TYPE || this.nbt.getType() == 0)
        {
            this.nbt.add(i, NbtInt.of(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, int[] value)
    {
        if (this.nbt.getType() == NbtElement.INT_ARRAY_TYPE || this.nbt.getType() == 0)
        {
            this.nbt.add(i, new NbtIntArray(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, List<Integer> value)
    {
        if (this.nbt.getType() == NbtElement.INT_ARRAY_TYPE || this.nbt.getType() == 0)
        {
            this.nbt.add(i, new NbtIntArray(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, double value)
    {
        if (this.nbt.getType() == NbtElement.DOUBLE_TYPE || this.nbt.getType() == 0)
        {
            this.nbt.add(i, NbtDouble.of(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, float value)
    {
        if (this.nbt.getType() == NbtElement.FLOAT_TYPE || this.nbt.getType() == 0)
        {
            this.nbt.add(i, NbtFloat.of(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, String value)
    {
        if (this.nbt.getType() == NbtElement.STRING_TYPE || this.nbt.getType() == 0)
        {
            this.nbt.add(i, NbtString.of(value));
            return true;
        }
        else return false;
    }
    //endregion
    //region Appending
    public boolean add(NBTWrapper<?> nbt) { return add(this.nbt.size(), nbt); }
    public boolean add(short value) { return add(nbt.size(), value); }
    public boolean add(int value) { return add(nbt.size(), value); }
    public boolean add(int[] value) { return add(nbt.size(), value); }
    public boolean add(List<Integer> value) { return add(nbt.size(), value); }
    public boolean add(double value) { return add(nbt.size(), value); }
    public boolean add(float value) { return add(nbt.size(), value); }
    public boolean add(String value) { return add(nbt.size(), value); }
    //endregion
}
