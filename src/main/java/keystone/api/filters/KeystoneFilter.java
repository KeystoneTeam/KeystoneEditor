package keystone.api.filters;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.WorldRegion;
import keystone.api.Keystone;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.Item;
import keystone.api.wrappers.entities.Entity;
import keystone.core.gui.widgets.inputs.fields.EditableObject;
import keystone.core.modules.selection.SelectionModule;
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
 * Contains information relating to which {@link WorldRegion FilterBoxes} the filter is modifying, as well
 * as several API functions
 */
public class KeystoneFilter extends EditableObject
{
    private String name;
    private boolean compiledSuccessfully;
    private WorldRegion[] regions;
    private int iteration;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Set the name of the filter
     * @param name The name of the filter
     * @return The modified filter instance
     */
    public final KeystoneFilter setName(String name) { this.name = name; return this; }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Mark the filter as successfully compiled
     * @return The modified filter instance
     */
    public final KeystoneFilter compiledSuccessfully() { this.compiledSuccessfully = true; return this; }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Set the {@link WorldRegion FilterBoxes} this filter is being run on
     * @param regions The regions that the filter is modifying
     * @return The modified filter instance
     */
    public final KeystoneFilter setBlockRegions(WorldRegion[] regions)
    {
        this.regions = regions;
        return this;
    }

    public final void setIteration(int iteration)
    {
        this.iteration = iteration;
    }
    //endregion
    //region Filter Steps
    /**
     * @return Whether to ignore blocks that have already been processed in another {@link WorldRegion}
     */
    public boolean ignoreRepeatBlocks() { return true; }

    /**
     * @return Whether to allow placing blocks outside the current {@link WorldRegion} the filter is
     * modifying. You should only enable this if the filter is meant for population, such as foresting
     */
    public boolean allowBlocksOutsideRegion() { return false; }

    /**
     * @return The number of times to run this filter on the selection
     */
    public int iterations() { return 1; }

    /**
     * Ran before {@link keystone.api.filters.KeystoneFilter#processRegion(WorldRegion)}. Use this to do any initialization that cannot
     * be done in the constructor
     */
    public void prepare() {}

    /**
     * Ran for every {@link WorldRegion} the filter is modifying. Use this for any modifications that
     * cannot be done on a per-block basis
     * @param region The {@link WorldRegion} that is being modified
     */
    public void processRegion(WorldRegion region) {}

    /**
     * Ran for every block the filter is modifying after {@link keystone.api.filters.KeystoneFilter#processRegion(WorldRegion)}. Use this
     * for modifications that can be done on a per-block basis. Be sure that this code is self-contained, as this will be ran on
     * multiple threads for improved performance, and as such is subject to race conditions
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param region The {@link WorldRegion} that the block is in
     */
    public void processBlock(int x, int y, int z, WorldRegion region)  {}

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
     * @return The current iteration this filter is processing
     */
    public final int iteration() { return iteration; }

    /**
     * Send a message to the player in in-game chat
     * @param message The message to send
     */
    protected final void print(Object message)
    {
        if (message == null) message = "null";
        Minecraft.getInstance().player.sendMessage(new StringTextComponent(message.toString()), Util.NIL_UUID);
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
     * Throw an exception and abort further execution
     * @param exception The exception to be thrown
     */
    protected final void throwException(Exception exception) { Keystone.filterException(this, exception); }
    /**
     * @return The number of {@link WorldRegion FilterBoxes} that the filter is modifying
     */
    protected final int regionCount() { return Keystone.getModule(SelectionModule.class).getSelectionBoxCount(); }

    /**
     * Create a {@link BlockPalette} from multiple block IDs. Any ID that is a valid ID
     * for the /setblock command will work. Add a number after the ID to specify the block's weight in the
     * palette, with a higher weight being more likely to be chosen. [e.g. "minecraft:stone_slab[type=top] 10"]
     * @param contents A list of block IDs and optionally weights
     * @return The {@link BlockPalette} that is generated
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
     * Create a {@link BlockPalette} from multiple {@link Block Blocks}
     * @param blocks A list of {@link Block Blocks}
     * @return The {@link BlockPalette} that is generated
     */
    protected final BlockPalette palette(Block... blocks)
    {
        BlockPalette palette = new BlockPalette();
        for (Block block : blocks) palette = palette.with(block);
        return palette;
    }

    /**
     * Create a blacklist {@link BlockMask} from multiple block IDs. Any ID that
     * is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param contents A list of block IDs
     * @return The blacklist {@link BlockMask}
     */
    protected final BlockMask blacklist(String... contents) { return whitelist(contents).blacklist(); }
    /**
     * Create a blacklist {@link BlockMask} from multiple {@link Block Blocks}
     * @param blocks A list of {@link Block Blocks}
     * @return The blacklist {@link BlockMask}
     */
    protected final BlockMask blacklist(Block... blocks) { return whitelist(blocks).blacklist(); }
    /**
     * Create a whitelist {@link BlockMask} from multiple block IDs. Any ID that
     * is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param contents A list of block IDs
     * @return The whitelist {@link BlockMask}
     */
    protected final BlockMask whitelist(String... contents)
    {
        BlockMask mask = new BlockMask();
        for (String content : contents) mask = mask.with(content);
        return mask;
    }
    /**
     * Create a whitelist {@link BlockMask} from multiple {@link Block Blocks}
     * @param blocks A list of {@link Block Blocks}
     * @return The whitelist {@link BlockMask} that is generated
     */
    protected final BlockMask whitelist(Block... blocks)
    {
        BlockMask mask = new BlockMask();
        for (Block block : blocks) mask = mask.with(block);
        return mask;
    }

    /**
     * Calculate the entry of multiple {@link BlockPalette BlockPalettes} at the same
     * random index. Use this matching related blocks, such as randomly selecting a log and leaf combo
     * @param palettes A list of {@link BlockPalette BlockPalettes}
     * @return An array containing the resolved {@link Block Blocks}, in the same order as the provided {@link BlockPalette BlockPalettes}
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
     * Create a {@link Block} from a block ID. Any ID that is a valid ID for the
     * /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID
     * @return The generated {@link Block}
     */
    public static final Block block(String block)
    {
        BlockState state = Blocks.RED_STAINED_GLASS.defaultBlockState();
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
    /**
     * Create an {@link Entity} from an entity ID. Any ID that is a valid ID for the
     * /summon command will work. [e.g. "minecraft:creeper"]
     * @param id The entity ID
     * @return The generated {@link Entity}
     */
    public static Entity entity(String id)
    {
        return new Entity(id);
    }
    //endregion
}
