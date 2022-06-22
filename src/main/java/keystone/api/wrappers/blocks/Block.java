package keystone.api.wrappers.blocks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;

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
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param block A string representing a block. Must be valid for {@link KeystoneFilter#block(String)}
     * @return The created block wrapper
     */
    public static Block create(String block)
    {
        BlockState state = Blocks.RED_STAINED_GLASS.getDefaultState();
        NbtCompound tileEntity = null;

        try
        {
            BlockArgumentParser.BlockResult parser = BlockArgumentParser.block(Registry.BLOCK, block, true);
            state = parser.blockState();
            tileEntity = parser.nbt();
        }
        catch (CommandSyntaxException e)
        {
            Keystone.tryCancelFilter(e.getLocalizedMessage());
        }

        return new Block(state, tileEntity);
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param state The Minecraft BlockState
     */
    public Block(@Nonnull BlockState state) { this(BlockTypeRegistry.fromMinecraftBlock(state), null); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param state The Minecraft BlockState
     * @param tileEntity The Minecraft BlockEntity
     */
    public Block(@Nonnull BlockState state, BlockEntity tileEntity) { this(BlockTypeRegistry.fromMinecraftBlock(state), tileEntity == null ? null : new NBTCompound(tileEntity.createNbt())); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param state The Minecraft BlockState
     * @param tileEntity The Minecraft NbtCompound representing the tile entity
     */
    public Block(@Nonnull BlockState state, NbtCompound tileEntity) { this(BlockTypeRegistry.fromMinecraftBlock(state), tileEntity == null ? null : new NBTCompound(tileEntity)); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param blockType The {@link BlockType}
     */
    public Block(@Nonnull BlockType blockType) { this(blockType, null); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
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
     * @return This Block instance, for use in function chaining
     */
    public Block setBlockType(@Nonnull BlockType type) { this.blockType = type; return this; }
    /**
     * Set this block's tile entity data
     * @param tileEntity The {@link NBTCompound} representing this tile entity
     * @return This Block instance, for use in function chaining
     */
    public Block setTileEntity(NBTCompound tileEntity) { this.tileEntity = tileEntity; return this; }
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
