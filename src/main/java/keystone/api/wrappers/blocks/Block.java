package keystone.api.wrappers.blocks;

import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Block
{
    private BlockType blockType;
    private NBTCompound tileEntity;

    public Block(@Nonnull BlockState state) { this(BlockTypeRegistry.fromMinecraftBlock(state), null); }
    public Block(@Nonnull BlockState state, TileEntity tileEntity) { this(BlockTypeRegistry.fromMinecraftBlock(state), tileEntity == null ? null : new NBTCompound(tileEntity.serializeNBT())); }
    public Block(@Nonnull BlockState state, CompoundNBT tileEntity) { this(BlockTypeRegistry.fromMinecraftBlock(state), tileEntity == null ? null : new NBTCompound(tileEntity)); }
    public Block(@Nonnull BlockType blockType) { this(blockType, null); }
    public Block(@Nonnull BlockType blockType, NBTCompound tileEntity)
    {
        this.blockType = blockType;
        this.tileEntity = tileEntity;
        if (this.tileEntity != null && this.tileEntity.getMinecraftNBT().isEmpty()) this.tileEntity = null;
    }

    //region API Getters
    public BlockType blockType() { return this.blockType; }
    public NBTCompound tileEntity()
    {
        if (this.tileEntity == null) this.tileEntity = new NBTCompound();
        return this.tileEntity;
    }
    //endregion
    //region API Setters
    public void setBlockType(@Nonnull BlockType type) { this.blockType = type; }
    public void setTileEntity(NBTCompound tileEntity) { this.tileEntity = tileEntity; }
    //endregion

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return blockType.equals(block.blockType) && Objects.equals(tileEntity, block.tileEntity);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(blockType, tileEntity);
    }
    @Override
    public String toString()
    {
        if (tileEntity == null) return blockType().toString();
        else return blockType.toString().concat(tileEntity.toString());
    }
}
