package keystone.api.wrappers;

import net.minecraft.item.ItemStack;

/**
 * A wrapper for a Minecraft item stack. Contains information about the item, count, and NBT data
 */
public class Item
{
    private ItemStack stack;

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param stack The Minecraft item stack
     */
    public Item(ItemStack stack)
    {
        this.stack = stack;
    }

    /**
     * Set the count of the {@link keystone.api.wrappers.Item}
     * @param count The count
     * @return The modified {@link keystone.api.wrappers.Item}
     */
    public Item count(int count)
    {
        return new Item(new ItemStack(stack.getItem(), count, stack.getTag()));
    }
    /**
     * Set the damage of the {@link keystone.api.wrappers.Item}
     * @param damage The damage
     * @return The modified {@link keystone.api.wrappers.Item}
     */
    public Item damage(int damage)
    {
        ItemStack newStack = new ItemStack(stack.getItem(), stack.getCount(), stack.getTag());
        newStack.setDamageValue(damage);
        return new Item(newStack);
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return The Minecraft item stack
     */
    public ItemStack getMinecraftItem() { return stack; }
}
