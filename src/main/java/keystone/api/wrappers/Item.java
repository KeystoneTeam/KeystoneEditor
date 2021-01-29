package keystone.api.wrappers;

import net.minecraft.item.ItemStack;

public class Item
{
    private ItemStack stack;

    public Item(ItemStack stack)
    {
        this.stack = stack;
    }

    public Item count(int count)
    {
        return new Item(new ItemStack(stack.getItem(), count, stack.getTag()));
    }
    public Item damage(int damage)
    {
        ItemStack newStack = new ItemStack(stack.getItem(), stack.getCount(), stack.getTag());
        newStack.setDamage(damage);
        return new Item(newStack);
    }

    public ItemStack getMinecraftItem() { return stack; }
}
