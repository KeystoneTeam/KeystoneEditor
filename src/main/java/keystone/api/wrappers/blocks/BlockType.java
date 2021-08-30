package keystone.api.wrappers.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.state.Property;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A wrapper for a Minecraft block. Contains information about the block's state and NBT data
 */
public class BlockType
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

    private short keystoneID;
    private BlockState state;
    private String string;
    private String block;
    private String properties;
    private String allProperties;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param keystoneID The Keystone Registry ID
     * @param state The Minecraft block state
     */
    public BlockType(short keystoneID, BlockState state)
    {
        this.keystoneID = keystoneID;
        this.state = state;
        buildStrings();
    }

    public short getKeystoneID() { return keystoneID; }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return This block's Minecraft block state
     */
    public BlockState getMinecraftBlock() { return state; }
    //endregion
    //region API
    /**
     * @return The ID of the base block. [e.g. "minecraft:stone_slab"]
     */
    public String block() { return state.getBlock().getRegistryName().toString(); }
    /**
     * @return This block's property set. [e.g. "type=top,waterlogged=true"]
     */
    public String properties() { return this.properties; }
    /**
     * @return This block's property set, including default values. [e.g. "type=top,waterlogged=false"]
     */
    public String allProperties() { return this.allProperties; }

    /**
     * Check whether this block's type is the same as another {@link BlockType}, regardless of their
     * property sets or tile entities
     * @param test The {@link BlockType} to test against
     * @return True if the both block's types are equal.
     */
    public boolean isBlock(BlockType test)
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
        return isBlock(KeystoneFilter.block(test).blockType());
    }

    /**
     * Apply a given property set to this block
     * @param properties A property set. [e.g. "type=top", "type=top,waterlogged=true"]
     * @return The modified block instance
     */
    public BlockType withProperties(String properties)
    {
        try
        {
            String blockStr = BlockStateParser.serialize(this.state);
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
            return BlockTypeRegistry.fromMinecraftBlock(parser.getState());
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
    public BlockType withProperty(String property, String value)
    {
        try
        {
            String blockStr = BlockStateParser.serialize(this.state);
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
            return BlockTypeRegistry.fromMinecraftBlock(parser.getState());
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
    //endregion
    //region Object Overrides
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass() == this.getClass())
        {
            BlockType blockType = (BlockType) o;
            return this.string.equals(blockType.string);
        }
        if (o.getClass() == String.class)
        {
            String string = (String) o;
            BlockType blockType = KeystoneFilter.block(string).blockType();
            return this.string.equals(blockType.string);
        }
        return false;
    }
    @Override
    public int hashCode()
    {
        return keystoneID;
    }

    @Override
    public String toString()
    {
        return this.string;
    }
    private void buildStrings()
    {
        // Full String
        StringBuilder stringbuilder = new StringBuilder(this.state.getBlock().getRegistryName().toString());
        if (!this.state.getValues().isEmpty())
        {
            stringbuilder.append('[');
            stringbuilder.append(this.state.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            stringbuilder.append(']');
        }
        this.string = stringbuilder.toString();
        
        // Block ID
        this.block = this.state.getBlock().getRegistryName().toString();
        
        // Modified Properties
        BlockState defaultState = this.state.getBlock().defaultBlockState();
        this.properties = this.state.getValues().entrySet().stream()
                .filter(entry -> PROPERTY_VALUE_DIFFERENT_PREDICATE.test(defaultState, entry))
                .map(PROPERTY_ENTRY_TO_STRING_FUNCTION)
                .collect(Collectors.joining(","));
        
        // All Properties
        this.allProperties = this.state.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(","));
    }
    //endregion
}
