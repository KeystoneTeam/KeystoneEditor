package keystone.api.filters;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;
import keystone.api.wrappers.Item;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class KeystoneFilter
{
    private static final Block air = new Block(Blocks.AIR.getDefaultState());

    private String name;
    private boolean compiledSuccessfully;

    public boolean ignoreRepeatBlocks() { return true; }
    public void prepare() {}
    public void processBox(FilterBox box) {}
    public void processBlock(int x, int y, int z, FilterBox box)  {}
    public void finished() {}

    //region API
    public final String getName() { return name; }
    public final KeystoneFilter setName(String name) { this.name = name; return this; }
    public final boolean isCompiledSuccessfully() { return compiledSuccessfully; }
    public final KeystoneFilter compiledSuccessfully() { this.compiledSuccessfully = true; return this; }

    protected final void print(Object message) { Minecraft.getInstance().player.sendMessage(new StringTextComponent(message.toString()), Util.DUMMY_UUID); }
    protected final void abort(String reason)
    {
        Keystone.abortFilter(reason);
    }
    protected final BlockPalette palette(String... contents)
    {
        BlockPalette palette = new BlockPalette();
        for (String content : contents)
        {
            String[] tokens = content.split(" ");
            if (tokens.length > 1)
            {
                String blockStr = tokens[0];
                for (int i = 1; i < tokens.length - 1; i++) blockStr += tokens[i];

                try
                {
                    int weight = Integer.parseInt(tokens[tokens.length - 1]);
                    palette = palette.with(blockStr, weight);
                }
                catch (NumberFormatException e)
                {
                    blockStr += tokens[tokens.length - 1];
                    palette = palette.with(blockStr);
                }
            }
            else palette = palette.with(tokens[0]);
        }
        return palette;
    }
    protected final BlockPalette palette(Block... blocks)
    {
        BlockPalette palette = new BlockPalette();
        for (Block block : blocks) palette = palette.with(block);
        return palette;
    }
    protected final BlockMask mask(String... contents)
    {
        BlockMask mask = new BlockMask();
        for (String content : contents) mask = mask.with(content);
        return mask;
    }
    protected final BlockMask mask(Block... blocks)
    {
        BlockMask mask = new BlockMask();
        for (Block block : blocks) mask = mask.with(block);
        return mask;
    }

    public static final Block block(String block)
    {
        BlockState state = Blocks.RED_STAINED_GLASS.getDefaultState();
        CompoundNBT tileEntity = null;

        try
        {
            BlockStateParser parser = new BlockStateParser(new StringReader(block), false).parse(true);
            state = parser.getState();
            tileEntity = parser.getNbt();
        }
        catch (CommandSyntaxException e)
        {
            Keystone.abortFilter(e.getLocalizedMessage());
        }

        return new Block(state, tileEntity);
    }
    public static final Item item(String item)
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

    public static final Block air() { return air; }
    //endregion
}
