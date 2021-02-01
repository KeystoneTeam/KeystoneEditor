package keystone.api.wrappers;

import keystone.api.filters.KeystoneFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BlockMask
{
    private List<Block> mask = new ArrayList<>();

    public BlockMask with(String block) { return with(KeystoneFilter.block(block)); }
    public BlockMask with(Block block)
    {
        if (!mask.contains(block)) mask.add(block);
        return this;
    }

    public boolean contains(Block block) { return mask.contains(block); }
    public void forEach(Consumer<Block> consumer) { mask.forEach(block -> consumer.accept(block)); }
}
