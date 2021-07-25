package keystone.api.wrappers.nbt;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class NBTList<T> extends NBTWrapper<ListNBT>
{
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param nbt The Minecraft INBT
     */
    public NBTList(ListNBT nbt) { super(nbt); }
    public NBTList() { super(new ListNBT()); }

    public NBTType listType() { return NBTType.fromMinecraftID(nbt.getId()); }
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
        if (this.nbt.getElementType() == nbt.getMinecraftNBT().getId() || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, nbt.getMinecraftNBT());
            return true;
        }
        else return false;
    }
    public boolean add(int i, short value)
    {
        if (this.nbt.getElementType() == Constants.NBT.TAG_SHORT || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, ShortNBT.valueOf(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, int value)
    {
        if (this.nbt.getElementType() == Constants.NBT.TAG_INT || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, IntNBT.valueOf(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, int[] value)
    {
        if (this.nbt.getElementType() == Constants.NBT.TAG_INT_ARRAY || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, new IntArrayNBT(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, List<Integer> value)
    {
        if (this.nbt.getElementType() == Constants.NBT.TAG_INT_ARRAY || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, new IntArrayNBT(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, double value)
    {
        if (this.nbt.getElementType() == Constants.NBT.TAG_DOUBLE || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, DoubleNBT.valueOf(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, float value)
    {
        if (this.nbt.getElementType() == Constants.NBT.TAG_FLOAT || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, FloatNBT.valueOf(value));
            return true;
        }
        else return false;
    }
    public boolean add(int i, String value)
    {
        if (this.nbt.getElementType() == Constants.NBT.TAG_STRING || this.nbt.getElementType() == 0)
        {
            this.nbt.add(i, StringNBT.valueOf(value));
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
