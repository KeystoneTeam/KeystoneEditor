package keystone.api.wrappers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.arguments.NBTTagArgument;
import net.minecraft.command.impl.data.DataCommand;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class Block
{
    private BlockState state;
    private CompoundNBT tileEntity;

    public Block(BlockState state) { this(state, (CompoundNBT)null); }
    public Block(BlockState state, TileEntity tileEntity) { this(state, tileEntity != null ? tileEntity.serializeNBT() : null); }
    public Block(BlockState state, CompoundNBT tileEntity)
    {
        this.state = state;
        if (tileEntity != null) this.tileEntity = tileEntity;
    }

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

    public boolean isAir() { return this.state.isAir(); }
    public BlockState getMinecraftBlock() { return state; }
    public CompoundNBT getTileEntityData() { return tileEntity; }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return state.equals(block.state) &&
                Objects.equals(tileEntity, block.tileEntity);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(state, tileEntity);
    }

    @Override
    public String toString()
    {
        if (this.tileEntity == null) return this.state.toString();
        else return this.state.toString() + this.tileEntity.toString();
    }
}
