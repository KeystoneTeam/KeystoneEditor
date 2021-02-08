package keystone.api.filters;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;
import keystone.api.wrappers.Item;
import keystone.modules.selection.SelectionModule;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

/**
 * A filter compilable by Keystone. All filters must contain a class which extends {@link keystone.api.filters.KeystoneFilter}.
 * Contains information relating to which {@link keystone.api.filters.FilterBox FilterBoxes} the filter is modifying, as well
 * as several API functions
 */
public class KeystoneFilter
{
    private static final Block air = new Block(Blocks.AIR.getDefaultState());

    private String name;
    private boolean compiledSuccessfully;
    private FilterBox[] boxes;

    //region Creation
    /**
     * Set the name of the filter
     * @param name The name of the filter
     * @return The modified filter instance
     */
    public final KeystoneFilter setName(String name) { this.name = name; return this; }

    /**
     * Mark the filter as successfully compiled
     * @return The modified filter instance
     */
    public final KeystoneFilter compiledSuccessfully() { this.compiledSuccessfully = true; return this; }

    /**
     * Set the {@link keystone.api.filters.FilterBox FilterBoxes} this filter is being run on
     * @param boxes The filter boxes that the filter is modifying
     * @return The modified filter instance
     */
    public final KeystoneFilter setFilterBoxes(FilterBox[] boxes) { this.boxes = boxes; return this; }
    //endregion
    //region Filter Steps
    /**
     * @return Whether to ignore blocks that have already been processed in another {@link keystone.api.filters.FilterBox}
     */
    public boolean ignoreRepeatBlocks() { return true; }

    /**
     * Ran before {@link keystone.api.filters.KeystoneFilter#processBox(FilterBox)}. Use this to do any initialization that cannot
     * be done in the constructor
     */
    public void prepare() {}

    /**
     * Ran for every {@link keystone.api.filters.FilterBox} the filter is modifying. Use this for any modifications that
     * cannot be done on a per-block basis
     * @param box The {@link keystone.api.filters.FilterBox} that is being modified
     */
    public void processBox(FilterBox box) {}

    /**
     * Ran for every block the filter is modifying after {@link keystone.api.filters.KeystoneFilter#processBox(FilterBox)}. Use this
     * for modifications that can be done on a per-block basis. Be sure that this code is self-contained, as this will be ran on
     * multiple threads for improved performance, and as such is subject to race conditions
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param box The {@link keystone.api.filters.FilterBox} that the block is in
     */
    public void processBlock(int x, int y, int z, FilterBox box)  {}

    /**
     * Ran after the filter has finished execution. Use this for any modifications that need to be done at the very end
     */
    public void finished() {}
    //endregion
    //region API
    /**
     * @return The name of the filter
     */
    public final String getName() { return name; }
    /**
     * @return Whether the filter was compiled successfully
     */
    public final boolean isCompiledSuccessfully() { return compiledSuccessfully; }

    /**
     * Send a message to the player in in-game chat
     * @param message The message to send
     */
    protected final void print(Object message)
    {
        if (message == null) message = "null";
        Minecraft.getInstance().player.sendMessage(new StringTextComponent(message.toString()), Util.DUMMY_UUID);
    }
    /**
     * Abort filter execution
     * @param reason The reason the filter cannot be completed
     */
    protected final void abort(String reason)
    {
        Keystone.abortFilter(reason);
    }
    /**
     * @return The number of {@link keystone.api.filters.FilterBox FilterBoxes} that the filter is modifiying
     */
    protected final int boxCount() { return Keystone.getModule(SelectionModule.class).getSelectionBoxCount(); }

    /**
     * Create a {@link keystone.api.wrappers.BlockPalette} from multiple block IDs. Any ID that is a valid ID
     * for the /setblock command will work. Add a number after the ID to specify the block's weight in the
     * palette, with a higher weight being more likely to be chosen. [e.g. "minecraft:stone_slab[type=top] 10"]
     * @param contents A list of block IDs and optionally weights
     * @return The {@link keystone.api.wrappers.BlockPalette} that is generated
     */
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

    /**
     * Create a {@link keystone.api.wrappers.BlockPalette} from multiple {@link keystone.api.wrappers.Block Blocks}
     * @param blocks A list of {@link keystone.api.wrappers.Block Blocks}
     * @return The {@link keystone.api.wrappers.BlockPalette} that is generated
     */
    protected final BlockPalette palette(Block... blocks)
    {
        BlockPalette palette = new BlockPalette();
        for (Block block : blocks) palette = palette.with(block);
        return palette;
    }

    /**
     * Create a blacklist {@link keystone.api.wrappers.BlockMask} from multiple block IDs. Any ID that
     * is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param contents A list of block IDs
     * @return The blacklist {@link keystone.api.wrappers.BlockMask}
     */
    protected final BlockMask blacklist(String... contents) { return whitelist(contents).blacklist(); }
    /**
     * Create a blacklist {@link keystone.api.wrappers.BlockMask} from multiple {@link keystone.api.wrappers.Block Blocks}
     * @param blocks A list of {@link keystone.api.wrappers.Block Blocks}
     * @return The blacklist {@link keystone.api.wrappers.BlockMask}
     */
    protected final BlockMask blacklist(Block... blocks) { return whitelist(blocks).blacklist(); }
    /**
     * Create a whitelist {@link keystone.api.wrappers.BlockMask} from multiple block IDs. Any ID that
     * is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param contents A list of block IDs
     * @return The whitelist {@link keystone.api.wrappers.BlockMask}
     */
    protected final BlockMask whitelist(String... contents)
    {
        BlockMask mask = new BlockMask();
        for (String content : contents) mask = mask.with(content);
        return mask;
    }
    /**
     * Create a whitelist {@link keystone.api.wrappers.BlockMask} from multiple {@link keystone.api.wrappers.Block Blocks}
     * @param blocks A list of {@link keystone.api.wrappers.Block Blocks}
     * @return The whitelist {@link keystone.api.wrappers.BlockMask} that is generated
     */
    protected final BlockMask whitelist(Block... blocks)
    {
        BlockMask mask = new BlockMask();
        for (Block block : blocks) mask = mask.with(block);
        return mask;
    }

    /**
     * Calculate the entry of multiple {@link keystone.api.wrappers.BlockPalette BlockPalettes} at the same
     * random index. Use this matching related blocks, such as randomly selecting a log and leaf combo
     * @param palettes A list of {@link keystone.api.wrappers.BlockPalette BlockPalettes}
     * @return An array containing the resolved {@link keystone.api.wrappers.Block Blocks}, in the same order as the provided {@link keystone.api.wrappers.BlockPalette BlockPalettes}
     */
    protected final Block[] resolvePalettes(BlockPalette... palettes)
    {
        Block[] ret = new Block[palettes.length];
        if (palettes.length == 0) return ret;

        int index = palettes[0].randomIndex();
        for (int i = 0; i < ret.length; i++) ret[i] = palettes[i].getBlock(index);
        return ret;
    }

    /**
     * Create a {@link keystone.api.wrappers.Block} from a block ID. Any ID that is a valid ID for the
     * /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID
     * @return The generated {@link keystone.api.wrappers.Block}
     */
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
    /**
     * Create a {@link keystone.api.wrappers.Item} from an item ID. Any ID that is a valid ID for the
     * /give command will work. [e.g. "minecraft:diamond"]
     * @param item The item ID
     * @return The generated {@link keystone.api.wrappers.Item}
     */
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
    //endregion
}
