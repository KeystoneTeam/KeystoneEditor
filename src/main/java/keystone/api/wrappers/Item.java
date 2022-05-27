package keystone.api.wrappers;

import keystone.api.Keystone;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A wrapper for a Minecraft item stack. Contains information about the item, count, and NBT data
 */
public class Item
{
    public static final Item EMPTY = new Item(ItemStack.EMPTY);

    private final ItemStack stack;

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
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
        ItemStack newStack = this.stack.copy();
        newStack.setCount(count);
        return new Item(newStack);
    }
    /**
     * Set the damage of the {@link keystone.api.wrappers.Item}
     * @param damage The damage
     * @return The modified {@link keystone.api.wrappers.Item}
     */
    public Item setDamage(int damage)
    {
        ItemStack newStack = this.stack.copy();
        newStack.setDamage(damage);
        return new Item(newStack);
    }
    /**
     * Damage the {@link keystone.api.wrappers.Item} by a given amount
     * @param damage The amount of damage to add to the {@link keystone.api.wrappers.Item}
     * @param useUnbreaking Whether to include the Unbreaking enchantment in the damage calculation
     * @param allowBreaking If true, then if the item breaks, it will return an empty {@link keystone.api.wrappers.Item}. Otherwise,
     *                      it will wrap the damage value around
     * @return The modified {@link keystone.api.wrappers.Item}
     */
    public Item damage(int damage, boolean useUnbreaking, boolean allowBreaking)
    {
        ItemStack newStack = this.stack.copy();

        if (useUnbreaking) newStack.damage(damage, Keystone.RANDOM, null);
        else newStack.setDamage(newStack.getDamage() + damage);

        while (newStack.getDamage() >= newStack.getMaxDamage())
        {
            if (allowBreaking) return Item.EMPTY;
            else newStack.setDamage(newStack.getDamage() - newStack.getMaxDamage());
        }

        return new Item(newStack);
    }
    /**
     * Repair the {@link keystone.api.wrappers.Item} by a given amount
     * @param damage The amount of damage to remove from the {@link keystone.api.wrappers.Item}
     * @param wrap If true, then if the item is fully repaired, it will wrap around to fully damaged
     * @return The modified {@link keystone.api.wrappers.Item}
     */
    public Item repair(int damage, boolean wrap)
    {
        ItemStack newStack = this.stack.copy();

        int newDamage = newStack.getDamage() - damage;
        while (newDamage < 0)
        {
            if (wrap) newDamage += newStack.getMaxDamage();
            else newDamage = 0;
        }
        newStack.setDamage(newDamage);

        return new Item(newStack);
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft item stack
     */
    public ItemStack getMinecraftItem() { return stack; }
}
