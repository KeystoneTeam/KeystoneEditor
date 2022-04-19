package keystone.api.wrappers.nbt;

import net.minecraft.nbt.NbtElement;

public class NBTWrapper<N extends NbtElement>
{
    protected final N nbt;

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param nbt The Minecraft NbtElement
     */
    protected NBTWrapper(N nbt)
    {
        this.nbt = nbt;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
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
