package keystone.api.wrappers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.arguments.NBTTagArgument;
import net.minecraft.command.impl.data.DataCommand;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A wrapper for a Minecraft block. Contains information about the block's state and NBT data
 */
public class Block
{
    private BlockState state;
    private CompoundNBT tileEntity;

    //region INTERNAL USE ONLY
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param state The Minecraft block state
     */
    public Block(BlockState state) { this(state, (CompoundNBT)null); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param state The Minecraft block state
     * @param tileEntity The Minecraft tile entity
     */
    public Block(BlockState state, TileEntity tileEntity) { this(state, tileEntity != null ? tileEntity.serializeNBT() : null); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param state The Minecraft block state
     * @param tileEntity The Minecraft NBT data
     */
    public Block(BlockState state, CompoundNBT tileEntity)
    {
        this.state = state;
        if (tileEntity != null) this.tileEntity = tileEntity;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return This block's Minecraft block state
     */
    public BlockState getMinecraftBlock() { return state; }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return This block's Minecraft NBT data
     */
    public CompoundNBT getTileEntityData() { return tileEntity; }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param block The block state to set
     */
    public void setMinecraftBlock(BlockState block) { this.state = block; }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param tileEntity The TileEntity instance to set
     */
    public void setTileEntity(TileEntity tileEntity) { this.tileEntity = tileEntity == null ? null : tileEntity.serializeNBT(); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param tileEntity The tile entity NBT to set
     */
    public void setTileEntity(CompoundNBT tileEntity) { this.tileEntity = tileEntity; }
    //endregion

    public Block clone()
    {
        return new Block(getMinecraftBlock(), getTileEntityData() != null ? getTileEntityData().copy() : null);
    }
    /**
     * Apply a given property set to this block
     * @param properties A property set. [e.g. "type=top", "type=top,waterlogged=true"]
     * @return The modified block instance
     */
    public Block properties(String properties)
    {
        try
        {
            Block copy = new Block(this.state, this.tileEntity != null ? this.tileEntity.copy() : null);

            String blockStr = BlockStateParser.toString(copy.state);
            String[] tokens = properties.split(",");

            for (String token : tokens)
            {
                String propertyName = token.split("=")[0];

                if (blockStr.contains(propertyName)) blockStr = blockStr.replaceFirst(propertyName + "=[^,\\]]*", token);
                else if (blockStr.contains("["))
                {
                    StringBuilder newBlockStr = new StringBuilder();
                    for (char c : blockStr.toCharArray())
                    {
                        if (c == ']') newBlockStr.append("," + token);
                        newBlockStr.append(c);
                    }
                    blockStr = newBlockStr.toString();
                }
                else blockStr = blockStr + "[" + token + "]";
            }

            BlockStateParser parser = new BlockStateParser(new StringReader(blockStr), false).parse(false);
            copy.state = parser.getState();
            return copy;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            Keystone.abortFilter("Malformed block properties set " + properties);
            return this;
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
            return this;
        }
    }
    /**
     * Apply a given property value to this block
     * @param property The property to set. [e.g. "type"]
     * @param value The value of the property. [e.g. "top"]
     * @return The modified block instance
     */
    public Block property(String property, String value)
    {
        try
        {
            Block copy = new Block(this.state, this.tileEntity != null ? this.tileEntity.copy() : null);

            String blockStr = BlockStateParser.toString(copy.state);
            if (blockStr.contains(property)) blockStr = blockStr.replaceFirst("(" + property + ")=([^,\\]]*)", "$1=" + value);
            else if (blockStr.contains("["))
            {
                StringBuilder newBlockStr = new StringBuilder();
                for (char c : blockStr.toCharArray())
                {
                    if (c == ']') newBlockStr.append("," + property + "=" + value);
                    newBlockStr.append(c);
                }
                blockStr = newBlockStr.toString();
            }
            else blockStr = blockStr + "[" + property + "=" + value + "]";

            BlockStateParser parser = new BlockStateParser(new StringReader(blockStr), false).parse(false);
            copy.state = parser.getState();
            return copy;
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
            return this;
        }
    }

    /**
     * Set NBT data at a given path to a given value
     * @param path The NBT path. [e.g. "Items[0].Count", "Items[{Slot:0b}]"]
     * @param data The value to set. [e.g. "32b", "{id:"minecraft:diamond",Count:2b}"]
     * @return The modified block instance
     */
    public Block data(String path, String data)
    {
        try
        {
            Block copy = new Block(this.state, this.tileEntity != null ? this.tileEntity.copy() : new CompoundNBT());

            NBTPathArgument.NBTPath nbtPath = NBTPathArgument.nbtPath().parse(new StringReader(path));
            INBT nbt = NBTTagArgument.func_218085_a().parse(new StringReader(data));
            nbtPath.func_218076_b(copy.tileEntity, () -> nbt);

            return copy;
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
            return this;
        }
    }

    /**
     * @return Whether this block is an air block
     */
    public boolean isAir() { return this.state.isAir(); }

    /**
     * @return Whether this block is a liquid
     */
    public boolean isLiquid() { return this.state.getBlock() instanceof FlowingFluidBlock; }

    /**
     * @return Whether this block is an air block or a liquid
     */
    public boolean isAirOrLiquid() { return isAir() || isLiquid(); }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return toString().equals(block.toString());
    }
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }
    @Override
    public String toString()
    {
        if (this.tileEntity == null) return this.state.toString();
        else return this.state.toString() + this.tileEntity.toString();
    }
}
