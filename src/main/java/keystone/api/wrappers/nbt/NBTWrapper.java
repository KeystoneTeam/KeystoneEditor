package keystone.api.wrappers.nbt;

import net.minecraft.nbt.NbtElement;

public class NBTWrapper<N extends NbtElement>
{
    protected final N nbt;

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param nbt The Minecraft NbtElement
     */
    protected NBTWrapper(N nbt)
    {
        this.nbt = nbt;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft NbtElement
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
        return nbt.asString();
    }
}
