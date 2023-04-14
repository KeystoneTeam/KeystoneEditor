package keystone.core.modules.filter.providers;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.state.property.Property;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;

import java.util.*;

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
        this.vagueProperties.putAll(vagueProperties);
        addTagContents(blockTag, vagueProperties);
        
        // Tag
        if (blockTag instanceof RegistryEntryList.Named<Block> namedTag) this.tag = namedTag.getTag();
    }

    @Override
    public BlockType get()
    {
        return BlockTypeRegistry.fromMinecraftBlock(states.get(Keystone.RANDOM.nextInt(states.size())));
    }
    @Override
    public BlockType getFirst()
    {
        return BlockTypeRegistry.fromMinecraftBlock(this.states.get(0));
    }
    @Override
    public IBlockProvider clone()
    {
        if (tag != null)
        {
            Optional<RegistryEntryList.Named<Block>> tagContents = Registry.BLOCK.getEntryList(this.tag);
            if (tagContents.isPresent())
            {
                Map<String, String> clonedProperties = new HashMap<>(vagueProperties);
                return new BlockListProvider(tagContents.get(), clonedProperties);
            }
        }
    
        BlockState[] statesArray = states.toArray(BlockState[]::new);
        return new BlockListProvider(statesArray);
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
            this.tag = TagKey.of(Registry.BLOCK_KEY, tagID);
            this.states.clear();
            Registry.BLOCK.getEntryList(this.tag).ifPresent(contents -> addTagContents(contents, this.vagueProperties));
        }
        
        // From States
        states.clear();
        NbtList statesNBT = nbt.getList("States", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < statesNBT.size(); i++) states.add(NbtHelper.toBlockState(statesNBT.getCompound(i)));
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
                if (property == null) Keystone.LOGGER.warn("Trying to set unknown vague property '" + propertyName + "' for block '" + Registry.BLOCK.getId(block) + "'!");
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
