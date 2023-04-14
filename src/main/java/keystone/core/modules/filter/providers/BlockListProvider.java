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
import net.minecraft.util.registry.RegistryEntryList;

import java.util.*;

public class BlockListProvider implements IBlockProvider
{
    private final List<BlockState> states;
    
    private BlockListProvider()
    {
        this.states = new ArrayList<>();
    }
    public BlockListProvider(BlockState... states)
    {
        this.states = new ArrayList<>();
        Collections.addAll(this.states, states);
    }
    public BlockListProvider(List<BlockState> states)
    {
        this.states = new ArrayList<>();
        this.states.addAll(states);
    }
    public BlockListProvider(RegistryEntryList<Block> blockTag, Map<String, String> vagueProperties)
    {
        List<BlockState> states = new ArrayList<>();
        blockTag.forEach(entry ->
        {
            // TODO: Implement vague property checking
            states.add(entry.value().getDefaultState());
        });
        this.states = Collections.unmodifiableList(states);
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
        BlockState[] statesArray = states.toArray(BlockState[]::new);
        return new BlockListProvider(statesArray);
    }
    
    @Override
    public NbtCompound write()
    {
        NbtCompound nbt = new NbtCompound();
        
        // States
        NbtList statesNBT = new NbtList();
        for (BlockState state : states) statesNBT.add(NbtHelper.fromBlockState(state));
        nbt.put("States", statesNBT);
        
        return nbt;
    }
    @Override
    public void read(NbtCompound nbt)
    {
        // States
        states.clear();
        NbtList statesNBT = nbt.getList("States", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < statesNBT.size(); i++) states.add(NbtHelper.toBlockState(statesNBT.getCompound(i)));
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
