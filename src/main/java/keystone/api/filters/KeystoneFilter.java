package keystone.api.filters;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.Item;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.entities.Entity;
import keystone.core.gui.widgets.inputs.fields.EditableObject;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;

/**
 * A filter compilable by Keystone. All filters must contain a class which extends {@link keystone.api.filters.KeystoneFilter}.
 * Contains information relating to which {@link WorldRegion FilterBoxes} the filter is modifying, as well
 * as several API functions
 */
public class KeystoneFilter extends EditableObject
{
    private String name;
    private boolean compiledSuccessfully;
    private Throwable compilerException;
    private WorldRegion[] regions;
    private int iteration;

    //region Static
    public static String getFilterName(File filterFile, boolean removeSpaces)
    {
        if (removeSpaces) return filterFile.getName().replaceAll(" ", "").replaceFirst("[.][^.]+$", "");
        else return filterFile.getName().replaceFirst("[.][^.]+$", "");
    }
    //endregion

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
     * Set the exception thrown when compiling the filter
     * @param exception The compiler exception
     * @return The modified filter instance
     */
    public final KeystoneFilter setCompilerException(Throwable exception) { this.compilerException = exception; return this; }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The exception thrown when compiling the filter
     */
    public final Throwable getCompilerException() { return this.compilerException; }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Set the {@link WorldRegion FilterBoxes} this filter is being run on
     * @param regions The regions that the filter is modifying
     * @return The modified filter instance
     */
    public final KeystoneFilter setBlockRegions(WorldRegion[] regions)
    {
        this.regions = regions;
        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        for (WorldRegion region : regions)
        {
            int minChunkX = region.min.x >> 4;
            int minChunkY = region.min.y >> 4;
            int minChunkZ = region.min.z >> 4;
            int maxChunkX = minChunkX + (int)Math.ceil(region.size.x / 16.0f);
            int maxChunkY = minChunkY + (int)Math.ceil(region.size.y / 16.0f);
            int maxChunkZ = minChunkZ + (int)Math.ceil(region.size.z / 16.0f);

            for (int chunkX = minChunkX; chunkX < maxChunkX; chunkX++)
            {
                for (int chunkY = minChunkY; chunkY < maxChunkY; chunkY++)
                {
                    for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++)
                    {
                        historyModule.getOpenEntry().preloadChunk(chunkX, chunkY, chunkZ);
                    }
                }
            }
        }
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
     * @return Whether to ignore entities that have already been processed in another {@link WorldRegion}
     */
    public boolean ignoreRepeatEntities() { return false; }
    /**
     * @return Whether to allow placing blocks outside the current {@link WorldRegion} the filter is
     * modifying. You should only enable this if the filter is meant for population, such as foresting
     */
    public boolean allowBlocksOutsideRegion() { return false; }

    /**
     * Ran at the very beginning of filter execution, before calculating iterations or processing region content. Use
     * this to do any initialization that cannot be done in the constructor
     */
    public void initialize() {}
    /**
     * @return The number of times to run this filter on the selection
     */
    public int iterations() { return 1; }
    /**
     * Ran at the very beginning of a filter iteration. Use this to do any initialization for the current filter pass
     */
    public void preparePass() {}
    /**
     * Ran before {@link KeystoneFilter#getRegionSteps(WorldRegion)}. Use this to do calculations before counting the region's progress
     * bar steps
     * @param region The region to prepare
     */
    public void prepareRegion(WorldRegion region) {}
    /**
     * @param region The region to check
     * @return The number of Progress Bar steps to process a region
     */
    public int getRegionSteps(WorldRegion region) { return 0; }
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
     * Ran for every entity the filter is modifying after {@link KeystoneFilter#processRegion(WorldRegion)}. Use this for modifications
     * that can be done on a per-entity basis. Be sure that this code is self-contained, as this will be ran on multiple threads
     * for improved performance, and as such is subject to race conditions
     * @param entity The {@link Entity} that is being modified
     * @param region The {@link WorldRegion} that the entity is in
     */
    public void processEntity(Entity entity, WorldRegion region) {}
    /**
     * Ran after a filter iteration has finished execution. Use this for any modifications that need to be done at the
     * very end of each filter pass
     */
    public void finishPass() {}
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
    public final void print(Object message)
    {
        if (message == null) message = "null";
        Minecraft.getInstance().player.sendMessage(new StringTextComponent(message.toString()), Util.NIL_UUID);
    }
    /**
     * Abort filter execution
     * @param reason The reason the filter cannot be completed
     */
    public final void abort(String reason)
    {
        Keystone.abortFilter(reason);
    }

    /**
     * Throw an throwable and abort further execution
     * @param throwable The throwable to be thrown
     */
    public final void throwException(Throwable throwable) { Keystone.filterException(this, throwable); }
    /**
     * @return The number of {@link WorldRegion FilterBoxes} that the filter is modifying
     */
    public final int regionCount() { return Keystone.getModule(SelectionModule.class).getSelectionBoxCount(); }

    /**
     * Create a {@link BlockPalette} from multiple block IDs. Any ID that is a valid ID
     * for the /setblock command will work. Add a number after the ID to specify the block's weight in the
     * palette, with a higher weight being more likely to be chosen. [e.g. "minecraft:stone_slab[type=top] 10"]
     * @param contents A list of block IDs and optionally weights
     * @return The {@link BlockPalette} that is generated
     */
    public final BlockPalette palette(String... contents)
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
     * Create a {@link BlockPalette} from multiple {@link BlockType Blocks}
     * @param blockTypes A list of {@link BlockType Blocks}
     * @return The {@link BlockPalette} that is generated
     */
    public final BlockPalette palette(BlockType... blockTypes)
    {
        BlockPalette palette = new BlockPalette();
        for (BlockType blockType : blockTypes) palette = palette.with(blockType);
        return palette;
    }

    /**
     * Create an empty blacklist {@link BlockMask}
     * @return The blacklist {@link BlockMask}
     */
    public final BlockMask blacklist() { return new BlockMask().blacklist(); }
    /**
     * Create a blacklist {@link BlockMask} from multiple block IDs. Any ID that
     * is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param contents A list of block IDs
     * @return The blacklist {@link BlockMask}
     */
    public final BlockMask blacklist(String... contents) { return whitelist(contents).blacklist(); }
    /**
     * Create a blacklist {@link BlockMask} from multiple {@link BlockType Blocks}
     * @param blockTypes A list of {@link BlockType Blocks}
     * @return The blacklist {@link BlockMask}
     */
    public final BlockMask blacklist(BlockType... blockTypes) { return whitelist(blockTypes).blacklist(); }
    /**
     * Create an empty whitelist {@link BlockMask}
     * @return The whitelist {@link BlockMask}
     */
    public final BlockMask whitelist() { return new BlockMask(); }
    /**
     * Create a whitelist {@link BlockMask} from multiple block IDs. Any ID that
     * is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param contents A list of block IDs
     * @return The whitelist {@link BlockMask}
     */
    public final BlockMask whitelist(String... contents)
    {
        BlockMask mask = new BlockMask();
        for (String content : contents) mask = mask.with(content);
        return mask;
    }
    /**
     * Create a whitelist {@link BlockMask} from multiple {@link BlockType Blocks}
     * @param blockTypes A list of {@link BlockType Blocks}
     * @return The whitelist {@link BlockMask} that is generated
     */
    public final BlockMask whitelist(BlockType... blockTypes)
    {
        BlockMask mask = new BlockMask();
        for (BlockType blockType : blockTypes) mask = mask.with(blockType);
        return mask;
    }

    /**
     * Calculate the entry of multiple {@link BlockPalette BlockPalettes} at the same
     * random index. Use this matching related blocks, such as randomly selecting a log and leaf combo
     * @param palettes A list of {@link BlockPalette BlockPalettes}
     * @return An array containing the resolved {@link BlockType Blocks}, in the same order as the provided {@link BlockPalette BlockPalettes}
     */
    public final BlockType[] resolvePalettes(BlockPalette... palettes)
    {
        BlockType[] ret = new BlockType[palettes.length];
        if (palettes.length == 0) return ret;

        int index = palettes[0].randomIndex();
        for (int i = 0; i < ret.length; i++) ret[i] = palettes[i].getBlockType(index);
        return ret;
    }

    /**
     * Create a {@link BlockType} from a block ID. Any ID that is a valid ID for the
     * /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID
     * @return The generated {@link BlockType}
     */
    public static Block block(String block)
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
        if (ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(id))) return new Entity(id);
        else
        {
            Keystone.abortFilter("Invalid entity ID: '" + id + "'!");
            return null;
        }
    }
    /**
     * Create a {@link Biome} from a biome ID. Any ID that is a valid ID for the
     * /locatebiome command will work. [e.g. "minecraft:plains"]
     * @param id The biome ID
     * @return The generated {@link Biome}
     */
    public static Biome biome(String id)
    {
        ResourceLocation location = new ResourceLocation(id);
        if (ForgeRegistries.BIOMES.containsKey(location)) return new Biome(ForgeRegistries.BIOMES.getValue(location));
        else
        {
            Keystone.abortFilter("Invalid biome ID: '" + id + "'!");
            return null;
        }
    }
    //endregion
}
