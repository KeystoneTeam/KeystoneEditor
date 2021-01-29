package keystone.api.filters;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.Item;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.ItemStack;

public class KeystoneFilter
{
    public boolean ignoreRepeatBlocks() { return true; }
    public void processBox(FilterBox box) {}
    public void processBlock(int x, int y, int z, FilterBox box)  {}

    //region API
    protected void abort(String reason)
    {
        Keystone.abortFilter(reason);
    }

    protected Block block(String block)
    {
        BlockState state = Blocks.RED_STAINED_GLASS.getDefaultState();

        try
        {
            BlockStateParser parser = new BlockStateParser(new StringReader(block), false).parse(true);
            state = parser.getState();
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
        }

        return new Block(state);
    }
    protected Item item(String item)
    {
        ItemStack stack = ItemStack.EMPTY;

        try
        {
            ItemParser parser = new ItemParser(new StringReader(item), false).parse();
            stack = new ItemStack(parser.getItem(), 1, parser.getNbt());
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
        }

        return new Item(stack);
    }
    //endregion
}
