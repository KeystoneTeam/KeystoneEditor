package keystone.api.wrappers.blocks;

import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A wrapper containing information on a block's type and tile entity data
 */
public class Block
{
    private BlockType blockType;
    private NBTCompound tileEntity;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param state The Minecraft BlockState
     */
    public Block(@Nonnull BlockState state) { this(BlockTypeRegistry.fromMinecraftBlock(state), null); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param state The Minecraft BlockState
     * @param tileEntity The Minecraft TileEntity
     */
    public Block(@Nonnull BlockState state, TileEntity tileEntity) { this(BlockTypeRegistry.fromMinecraftBlock(state), tileEntity == null ? null : new NBTCompound(tileEntity.serializeNBT())); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param state The Minecraft BlockState
     * @param tileEntity The Minecraft CompoundNBT representing the tile entity
     */
    public Block(@Nonnull BlockState state, CompoundNBT tileEntity) { this(BlockTypeRegistry.fromMinecraftBlock(state), tileEntity == null ? null : new NBTCompound(tileEntity)); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param blockType The {@link BlockType}
     */
    public Block(@Nonnull BlockType blockType) { this(blockType, null); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param blockType The {@link BlockType}
     * @param tileEntity The {@link NBTCompound} representing the tile entity
     */
    public Block(@Nonnull BlockType blockType, NBTCompound tileEntity)
    {
        this.blockType = blockType;
        this.tileEntity = tileEntity;
        if (this.tileEntity != null && this.tileEntity.getMinecraftNBT().isEmpty()) this.tileEntity = null;
    }
    //endregion
    //region API Getters
    /**
     * @return The {@link BlockType} of this block
     */
    public BlockType blockType() { return this.blockType; }
    /**
     * @return The {@link NBTCompound} representing this block's tile entity, or null
     */
    public NBTCompound tileEntity()
    {
        if (this.tileEntity == null) this.tileEntity = new NBTCompound();
        return this.tileEntity;
    }
    //endregion
    //region API Setters
    /**
     * Set this block's {@link BlockType}
     * @param type The new {@link BlockType}
     */
    public void setBlockType(@Nonnull BlockType type) { this.blockType = type; }
    /**
     * Set this block's tile entity data
     * @param tileEntity The {@link NBTCompound} representing this tile entity
     */
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
