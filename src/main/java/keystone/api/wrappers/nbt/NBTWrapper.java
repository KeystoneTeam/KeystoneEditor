package keystone.api.wrappers.nbt;

import net.minecraft.nbt.INBT;

public class NBTWrapper<N extends INBT>
{
    protected final N nbt;

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param nbt The Minecraft INBT
     */
    protected NBTWrapper(N nbt)
    {
        this.nbt = nbt;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return The Minecraft INBT
     */
    public N getMinecraftNBT() { return this.nbt; }

    @Override
    public boolean equals(Object obj)
    {
        return nbt.equals(obj);
    }
    @Override
    public int hashCode()
    {
        return nbt.hashCode();
    }
    @Override
    public String toString()
    {
        return nbt.getAsString();
    }
}
