package keystone.api.filters;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.Item;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.gui.widgets.inputs.fields.EditableObject;
import keystone.core.modules.filter.execution.CustomFilterThread;
import keystone.core.modules.filter.execution.FilterExecutor;
import keystone.core.modules.filter.execution.IFilterThread;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.utils.WorldRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryWrapper;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.io.File;
import java.util.Optional;

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
     * Set the current pass of this filter
     * @param pass The current pass number
     */
    public final void setPass(int pass)
    {
        this.iteration = pass;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * This must be called from a filter thread. If it is not, it will return null
     * @return The {@link FilterExecutor} running this filter
     */
    private FilterExecutor getExecutor()
    {
        if (Thread.currentThread() instanceof IFilterThread filterThread) return filterThread.getExecutor();
        else return null;
    }
    //endregion
    //region Filter Steps
    /**
     * Ran at before filter execution. Use this to tell Keystone how this filter is meant to be run
     * @param settings
     */
    public void createExecutionSettings(FilterExecutionSettings settings) { }
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
        MinecraftClient.getInstance().player.sendMessage(Text.literal(message.toString()), false);
    }
    /**
     * Cancel filter execution
     * @param reasons The reason the filter cannot be completed
     */
    public final void cancel(String... reasons)
    {
        getExecutor().cancel(reasons);
    }
    /**
     * Throw a throwable and abort further execution
     * @param throwable The throwable to be thrown
     */
    public final void throwException(Throwable throwable) { getExecutor().throwException(throwable); }

    /**
     * When writing loops with no guaranteed stopping point, always add a check for this at the beginning of each
     * iteration, to ensure the cancel button works. [e.g. if(isCancelled()) break;]
     * @return True if the filter was cancelled, false otherwise
     */
    public final boolean isCancelled() { return getExecutor().isCancelled(); }
    /**
     * @return The number of {@link WorldRegion FilterBoxes} that the filter is modifying
     */
    public final int regionCount() { return Keystone.getModule(SelectionModule.class).getSelectionBoxCount(); }

    /**
     * Create a new filter thread for running custom code
     * @param threadCode The code to run on the thread
     * @return The created thread
     */
    public CustomFilterThread thread(Runnable threadCode) { return getExecutor().newThread(threadCode, null, false); }
    /**
     * Create a new filter thread for running custom code
     * @param threadCode The code to run on the thread
     * @param onExecutionEnded The code to run when the rest of the code has finished
     * @return The created thread
     */
    public CustomFilterThread thread(Runnable threadCode, Runnable onExecutionEnded) { return getExecutor().newThread(threadCode, onExecutionEnded, false); }
    /**
     * Create a new filter thread for running custom code
     * @param threadCode The code to run on the thread
     * @param start If true, the thread starts automatically
     * @return The created thread
     */
    public CustomFilterThread thread(Runnable threadCode, boolean start) { return getExecutor().newThread(threadCode, null, start); }
    /**
     * Create a new filter thread for running custom code
     * @param threadCode The code to run on the thread
     * @param onExecutionEnded The code to run when the rest of the code has finished
     * @param start If true, the thread starts automatically
     * @return The created thread
     */
    public CustomFilterThread thread(Runnable threadCode, Runnable onExecutionEnded, boolean start) { return getExecutor().newThread(threadCode, onExecutionEnded, start); }

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
     * Create a {@link Block} from a block ID. Any ID that is a valid ID for the
     * /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID
     * @return The generated {@link Block}
     */
    public final Block block(String block)
    {
        return Block.create(block);
    }
    /**
     * Create a {@link Block} from a block ID and tile entity. Any ID that is a valid ID
     * for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID
     * @param tileEntity The tile entity
     * @return The generated {@link Block}
     */
    public final Block block(String block, NBTCompound tileEntity)
    {
        return block(block).setTileEntity(tileEntity);
    }
    /**
     * Create a {@link Block} from a block ID. Any ID that is a valid ID for the
     * /setblock command without NBT will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param blockType The block ID
     * @return The generated {@link BlockType}
     */
    public final BlockType blockType(String blockType)
    {
        BlockState state = Blocks.RED_STAINED_GLASS.getDefaultState();

        try
        {
            BlockArgumentParser.BlockResult parser = BlockArgumentParser.block(Registry.BLOCK, blockType, false);
            state = parser.blockState();
        }
        catch (CommandSyntaxException e)
        {
            getExecutor().cancel(e.getLocalizedMessage());
        }

        return BlockTypeRegistry.fromMinecraftBlock(state);
    }
    /**
     * Create a {@link keystone.api.wrappers.Item} from an item ID. Any ID that is a valid ID for the
     * /give command will work. [e.g. "minecraft:diamond"]
     * @param item The item ID
     * @return The generated {@link keystone.api.wrappers.Item}
     */
    public final Item item(String item)
    {
        ItemStack stack = ItemStack.EMPTY;

        try
        {
            ItemStringReader.ItemResult parser = ItemStringReader.item(CommandRegistryWrapper.of(Registry.ITEM), new StringReader(item));
            stack = new ItemStack(parser.item(), 1);
            stack.setNbt(parser.nbt());
        }
        catch (CommandSyntaxException e)
        {
            getExecutor().cancel(e.getLocalizedMessage());
        }

        return new Item(stack);
    }
    /**
     * Create an {@link Entity} from an entity ID. Any ID that is a valid ID for the
     * /summon command will work. [e.g. "minecraft:creeper"]
     * @param id The entity ID
     * @return The generated {@link Entity}
     */
    public final Entity entity(String id)
    {
        Optional<net.minecraft.entity.EntityType<?>> optionalEntity = Registry.ENTITY_TYPE.getOrEmpty(new Identifier(id));
        if (optionalEntity.isPresent()) return new Entity(id);
        else
        {
            getExecutor().cancel("Invalid entity ID: '" + id + "'!");
            return null;
        }
    }
    /**
     * Create a {@link Biome} from a biome ID. Any ID that is a valid ID for the
     * /locatebiome command will work. [e.g. "minecraft:plains"]
     * @param id The biome ID
     * @return The generated {@link Biome}
     */
    public final Biome biome(String id)
    {
        Registry<net.minecraft.world.biome.Biome> biomeRegistry = WorldRegistries.getBiomeRegistry();
        Optional<net.minecraft.world.biome.Biome> optionalBiome = biomeRegistry.getOrEmpty(new Identifier(id));
        if (optionalBiome.isPresent())
        {
            Optional<RegistryKey<net.minecraft.world.biome.Biome>> optionalBiomeKey = biomeRegistry.getKey(optionalBiome.get());
            if (optionalBiomeKey.isPresent()) return new Biome(biomeRegistry.getEntry(optionalBiomeKey.get()).get());
            else
            {
                getExecutor().cancel("Invalid biome entry ID: '" + id + "'!");
                return null;
            }
        }
        else
        {
            getExecutor().cancel("Invalid biome ID: '" + id + "'!");
            return null;
        }
    }
    /**
     * Create a schematic from two corners
     * @param corner1 The first corner
     * @param corner2 The second corner
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @return The generated {@link KeystoneSchematic}
     */
    public final KeystoneSchematic schematic(BlockPos corner1, BlockPos corner2, WorldModifierModules worldModifiers)
    {
        return KeystoneSchematic.createFromCorners(corner1.getMinecraftBlockPos(), corner2.getMinecraftBlockPos(), worldModifiers, RetrievalMode.LAST_SWAPPED, Blocks.STRUCTURE_VOID.getDefaultState());
    }
    /**
     * Create a schematic from two corners
     * @param corner1 The first corner
     * @param corner2 The second corner
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @return The generated {@link KeystoneSchematic}
     */
    public final KeystoneSchematic schematic(BlockPos corner1, BlockPos corner2, WorldModifierModules worldModifiers, RetrievalMode retrievalMode)
    {
        return KeystoneSchematic.createFromCorners(corner1.getMinecraftBlockPos(), corner2.getMinecraftBlockPos(), worldModifiers, retrievalMode, Blocks.STRUCTURE_VOID.getDefaultState());
    }
    /**
     * Create a schematic from two corners
     * @param corner1 The first corner
     * @param corner2 The second corner
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @param structureVoid The {@link BlockType} that represents structure voids
     * @return The generated {@link KeystoneSchematic}
     */
    public final KeystoneSchematic schematic(BlockPos corner1, BlockPos corner2, WorldModifierModules worldModifiers, RetrievalMode retrievalMode, BlockType structureVoid)
    {
        return KeystoneSchematic.createFromCorners(corner1.getMinecraftBlockPos(), corner2.getMinecraftBlockPos(), worldModifiers, retrievalMode, structureVoid.getMinecraftBlock());
    }
    /**
     * Create a schematic from a {@link BoundingBox}
     * @param bounds The {@link BoundingBox} of  the schematic
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @return The generated {@link KeystoneSchematic}
     */
    public final KeystoneSchematic schematic(BoundingBox bounds, WorldModifierModules worldModifiers)
    {
        BlockPos corner1 = new BlockPos((int)bounds.minX, (int)bounds.minY, (int)bounds.minZ);
        BlockPos corner2 = new BlockPos((int)bounds.maxX, (int)bounds.maxY, (int)bounds.maxZ);
        return KeystoneSchematic.createFromCorners(corner1.getMinecraftBlockPos(), corner2.getMinecraftBlockPos(), worldModifiers, RetrievalMode.ORIGINAL, Blocks.STRUCTURE_VOID.getDefaultState());
    }
    /**
     * Create a schematic from a {@link BoundingBox}
     * @param bounds The {@link BoundingBox} of  the schematic
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @return The generated {@link KeystoneSchematic}
     */
    public final KeystoneSchematic schematic(BoundingBox bounds, WorldModifierModules worldModifiers, RetrievalMode retrievalMode)
    {
        BlockPos corner1 = new BlockPos((int)bounds.minX, (int)bounds.minY, (int)bounds.minZ);
        BlockPos corner2 = new BlockPos((int)bounds.maxX, (int)bounds.maxY, (int)bounds.maxZ);
        return KeystoneSchematic.createFromCorners(corner1.getMinecraftBlockPos(), corner2.getMinecraftBlockPos(), worldModifiers, retrievalMode, Blocks.STRUCTURE_VOID.getDefaultState());
    }
    /**
     * Create a schematic from a {@link BoundingBox}
     * @param bounds The {@link BoundingBox} of  the schematic
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @param structureVoid The {@link BlockType} that represents structure voids
     * @return The generated {@link KeystoneSchematic}
     */
    public final KeystoneSchematic schematic(BoundingBox bounds, WorldModifierModules worldModifiers, RetrievalMode retrievalMode, BlockType structureVoid)
    {
        BlockPos corner1 = new BlockPos((int)bounds.minX, (int)bounds.minY, (int)bounds.minZ);
        BlockPos corner2 = new BlockPos((int)bounds.maxX, (int)bounds.maxY, (int)bounds.maxZ);
        return KeystoneSchematic.createFromCorners(corner1.getMinecraftBlockPos(), corner2.getMinecraftBlockPos(), worldModifiers, retrievalMode, structureVoid.getMinecraftBlock());
    }
    //endregion
}
