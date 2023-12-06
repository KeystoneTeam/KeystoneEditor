package keystone.core.modules.filter.blocks;

import keystone.api.Keystone;
import keystone.api.utils.StringUtils;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.BlockUtils;
import keystone.core.utils.WorldRegistries;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Property;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import java.util.*;
import java.util.function.Consumer;

public class BlockListProvider implements IBlockProvider
{
    private final List<BlockState> states = new ArrayList<>();
    private final Map<String, String> vagueProperties = new HashMap<>();
    private TagKey<Block> tag;
    
    private BlockListProvider() { }
    public BlockListProvider(BlockState... states)
    {
        Collections.addAll(this.states, states);
    }
    public BlockListProvider(List<BlockState> states)
    {
        this.states.addAll(states);
    }
    public BlockListProvider(RegistryEntryList<Block> blockTag, Map<String, String> vagueProperties)
    {
        // States
        if (vagueProperties != null) this.vagueProperties.putAll(vagueProperties);
        addTagContents(blockTag, this.vagueProperties);
        
        // Tag
        if (blockTag instanceof RegistryEntryList.Named<Block> namedTag) this.tag = namedTag.getTag();
    }

    @Override public BlockType get() { return BlockTypeRegistry.fromMinecraftBlock(states.get(Keystone.RANDOM.nextInt(states.size()))); }
    @Override public BlockType getFirst() { return BlockTypeRegistry.fromMinecraftBlock(this.states.get(0)); }
    @Override public void forEach(Consumer<BlockType> consumer) { this.states.forEach(state -> consumer.accept(BlockTypeRegistry.fromMinecraftBlock(state))); }
    @Override
    public IBlockProvider clone()
    {
        if (tag != null)
        {
            Optional<RegistryEntryList.Named<Block>> tagContents = Registries.BLOCK.getEntryList(this.tag);
            if (tagContents.isPresent())
            {
                Map<String, String> clonedProperties = new HashMap<>(vagueProperties);
                return new BlockListProvider(tagContents.get(), clonedProperties);
            }
        }
    
        BlockState[] statesArray = states.toArray(BlockState[]::new);
        return new BlockListProvider(statesArray);
    }
    
    @Override public boolean containsState(BlockType blockType) { return states.contains(blockType.getMinecraftBlock()); }
    @Override
    public boolean containsBlock(BlockType block)
    {
        for (BlockState state : this.states) if (state.getBlock().equals(block.getMinecraftBlock().getBlock())) return true;
        return false;
    }
    
    @Override
    public NbtCompound write()
    {
        NbtCompound nbt = new NbtCompound();
        
        // From Tag
        if (tag != null)
        {
            // Tag
            nbt.putString("Tag", tag.id().toString());
            
            // Vague Properties
            if (vagueProperties.size() > 0)
            {
                NbtCompound propertiesNBT = new NbtCompound();
                for (Map.Entry<String, String> property : vagueProperties.entrySet()) propertiesNBT.putString(property.getKey(), property.getValue());
                nbt.put("VagueProperties", propertiesNBT);
            }
        }

        // From States
        else
        {
            NbtList statesNBT = new NbtList();
            for (BlockState state : states) statesNBT.add(NbtHelper.fromBlockState(state));
            nbt.put("States", statesNBT);
        }
        
        return nbt;
    }
    @Override
    public void read(NbtCompound nbt)
    {
        // From Tag
        if (nbt.contains("Tag", NbtElement.STRING_TYPE))
        {
            // Vague Properties
            this.vagueProperties.clear();
            if (nbt.contains("VagueProperties", NbtElement.COMPOUND_TYPE))
            {
                NbtCompound propertiesNBT = nbt.getCompound("VagueProperties");
                for (String property : propertiesNBT.getKeys()) this.vagueProperties.put(property, propertiesNBT.getString(property));
            }
            
            // Tag
            Identifier tagID = new Identifier(nbt.getString("Tag"));
            this.tag = TagKey.of(RegistryKeys.BLOCK, tagID);
            this.states.clear();
            Registries.BLOCK.getEntryList(this.tag).ifPresent(contents -> addTagContents(contents, this.vagueProperties));
        }
        
        // From States
        else
        {
            states.clear();
            NbtList statesNBT = nbt.getList("States", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < statesNBT.size(); i++) states.add(NbtHelper.toBlockState(WorldRegistries.blockLookup(), statesNBT.getCompound(i)));
        }
    }
    
    private void addTagContents(RegistryEntryList<Block> tagContents, Map<String, String> vagueProperties)
    {
        tagContents.forEach(entry ->
        {
            // Load Default Block State
            Block block = entry.value();
            BlockState state = block.getDefaultState();
    
            // Load Vague Properties
            for (Map.Entry<String, String> vagueProperty : vagueProperties.entrySet())
            {
                String propertyName = vagueProperty.getKey();
                String propertyValue = vagueProperty.getValue();
        
                Property<?> property = block.getStateManager().getProperty(propertyName);
                if (property == null) Keystone.LOGGER.warn("Trying to set unknown vague property '" + propertyName + "' for block '" + Registries.BLOCK.getId(block) + "'!");
                else state = parsePropertyValue(state, property, propertyValue);
            }
            
            // Add State
            states.add(state);
        });
    }
    private <T extends Comparable<T>> BlockState parsePropertyValue(BlockState state, Property<T> property, String value)
    {
        Optional<T> parsed = property.parse(value);
        if (parsed.isPresent()) return state.with(property, parsed.get());
        else
        {
            Keystone.LOGGER.warn("Failed to parse vague property value '" + value + "' for property '" + property.getName() + "'!");
            return state;
        }
    }
    
    @Override
    public Text getName()
    {
        if (this.tag != null) return Text.literal("#" + this.tag.id().toString());
        else return Text.translatable("keystone.block_provider.dynamicList");
    }
    @Override
    public List<Text> getProperties()
    {
        List<Text> properties = new ArrayList<>();
        vagueProperties.forEach((name, value) -> properties.addAll(Text.literal(StringUtils.snakeCaseToTitleCase(name) + ": " + StringUtils.snakeCaseToTitleCase(value)).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY))));
        return properties;
    }
    @Override
    public ItemStack getDisplayItem()
    {
        // TODO: Cycle tag items in display
        Item item = BlockUtils.getBlockItem(getFirst().getMinecraftBlock().getBlock());
        return new ItemStack(item);
    }
    
    @Override
    public void openEditPropertiesScreen()
    {
        // TODO: Implement Vague Property Edit Menu
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockListProvider that = (BlockListProvider) o;
        return states.equals(that.states);
    }

    @Override
    public int hashCode()
    {
        return states.hashCode();
    }
}
