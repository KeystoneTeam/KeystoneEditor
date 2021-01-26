package keystone.api.block;

import keystone.api.Keystone;
import keystone.core.filters.FilterCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.state.Property;

public class Block
{
    private BlockState state;

    public Block(BlockState state)
    {
        this.state = state;
        FilterCache.setBlock(this);
    }

    public <T extends Comparable<T>, V extends T> Block with(String property, V value)
    {
        Property<T> propertyContainer = (Property<T>) state.getBlock().getStateContainer().getProperty(property);
        if (propertyContainer == null)
        {
            Keystone.LOGGER.error("Trying to set non-existant property '" + property + "' of block '" + state.getBlock().getRegistryName().toString() + "'!");
            return this;
        }

        if (propertyContainer.getAllowedValues().contains(value)) return new Block(state.with(propertyContainer, value));
        else Keystone.LOGGER.error("Trying to set property '" + property + "' of block '" + state.getBlock().getRegistryName().toString() + "' with invalid value '" + value.toString() + "'!");

        return this;
    }

    public BlockState getMinecraftBlock() { return state; }
}
