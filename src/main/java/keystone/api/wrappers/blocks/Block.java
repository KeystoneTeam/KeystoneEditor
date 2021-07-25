package keystone.api.wrappers.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.nbt.NBTCompound;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.arguments.NBTTagArgument;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A wrapper for a Minecraft block. Contains information about the block's state and NBT data
 */
public class Block
{
    protected static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Map.Entry<Property<?>, Comparable<?>>, String>()
    {
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry)
        {
            if (entry == null) return "<NULL>";
            else
            {
                Property<?> property = entry.getKey();
                return property.getName() + "=" + this.getName(property, entry.getValue());
            }
        }

        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) { return property.getName((T)comparable); }
    };
    protected static final BiPredicate<BlockState, Map.Entry<Property<?>, Comparable<?>>> PROPERTY_VALUE_DIFFERENT_PREDICATE = (defaultState, entry) ->
    {
        Comparable<?> defaultValue = defaultState.getValue(entry.getKey());
        return entry.getValue().equals(defaultValue);
    };

    private BlockState state;
    private CompoundNBT tileEntity;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
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
    //region API
    public Block clone()
    {
        return new Block(state, tileEntity != null ? tileEntity.copy() : null);
    }

    /**
     * @return The ID of the base block. [e.g. "minecraft:stone_slab"]
     */
    public String block() { return state.getBlock().getRegistryName().toString(); }
    /**
     * @return This block's property set. [e.g. "type=top,waterlogged=true"]
     */
    public String properties()
    {
        BlockState defaultState = this.state.getBlock().defaultBlockState();
        return this.state.getValues().entrySet().stream()
                .filter(entry -> PROPERTY_VALUE_DIFFERENT_PREDICATE.test(defaultState, entry))
                .map(PROPERTY_ENTRY_TO_STRING_FUNCTION)
                .collect(Collectors.joining(","));
    }
    /**
     * @return An {@link NBTCompound} representing this block's tile entity. Note that modifying
     * this NBT Compound will not modify the tile entity unless you call {@link Block#data(NBTCompound)}
     * once finished
     */
    public NBTCompound data()
    {
        if (this.tileEntity == null) return new NBTCompound();
        else return new NBTCompound(this.tileEntity.copy());
    }

    /**
     * Check whether this block's type is the same as another {@link Block}, regardless of their
     * property sets or tile entities
     * @param test The {@link Block} to test against
     * @return True if the both block's types are equal.
     */
    public boolean isBlock(Block test)
    {
        return test != null && block().equals(test.block());
    }
    /**
     * Check whether this block's type is the same as another block, regardless of their
     * property sets or tile entities
     * @param test The block ID to test against
     * @return True if the both block's types are equal.
     */
    public boolean isBlock(String test)
    {
        return isBlock(KeystoneFilter.block(test));
    }
    /**
     * Check whether this block's type and property set is the same as another {@link Block},
     * regardless of their tile entities
     * @param test The {@link Block} to test against
     * @return True if the both block's types and property sets are equal.
     */
    public boolean isBlockAndProperties(Block test)
    {
        return test != null && block().equals(test.block()) && properties().equals(test.properties());
    }
    /**
     * Check whether this block's type and property set is the same as another block,
     * regardless of their tile entities
     * @param test The block ID to test against
     * @return True if the both block's types and property sets are equal.
     */
    public boolean isBlockAndProperties(String test)
    {
        return isBlockAndProperties(KeystoneFilter.block(test));
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

            String blockStr = BlockStateParser.serialize(copy.state);
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
            Block copy = clone();

            String blockStr = BlockStateParser.serialize(copy.state);
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
            Block copy = clone();

            NBTPathArgument.NBTPath nbtPath = NBTPathArgument.nbtPath().parse(new StringReader(path));
            INBT nbt = NBTTagArgument.nbtTag().parse(new StringReader(data));
            nbtPath.set(copy.tileEntity, () -> nbt);

            return copy;
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
            return this;
        }
    }
    /**
     * Set this block's tile entity NBT data
     * @param nbt The {@link NBTCompound} that contains the new tile entity data
     * @return The modified block instance
     */
    public Block data(NBTCompound nbt)
    {
        Block copy = clone();
        copy.tileEntity = nbt.getMinecraftNBT();
        return copy;
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
    //endregion
    //region Object Overrides
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
        StringBuilder stringbuilder = new StringBuilder(this.state.getBlock().getRegistryName().toString());
        if (!this.state.getValues().isEmpty())
        {
            stringbuilder.append('[');
            stringbuilder.append(this.state.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            stringbuilder.append(']');
        }
        if (this.tileEntity != null) stringbuilder.append(this.tileEntity);
        return stringbuilder.toString();
    }
    //endregion
}
