package keystone.api.wrappers;

import keystone.api.filters.KeystoneFilter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BlockMask
{
    private List<Block> mask = new ArrayList<>();
    private boolean blacklist;

    public BlockMask clone()
    {
        BlockMask clone = new BlockMask();
        for (Block block : mask) clone.mask.add(new Block(block.getMinecraftBlock(), block.getTileEntityData()));
        clone.blacklist = blacklist;
        return clone;
    }

    public BlockMask with(String block)
    {
        if (block.startsWith("#"))
        {
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getCollection();
            ITag<net.minecraft.block.Block> tag = tags.get(new ResourceLocation(block.substring(1)));
            if (tag != null)
            {
                List<net.minecraft.block.Block> blocks = tag.getAllElements();
                for (net.minecraft.block.Block add : blocks) with(new Block(add.getDefaultState()));
            }
            return this;
        }
        else return with(KeystoneFilter.block(block));
    }
    public BlockMask with(Block block)
    {
        if (!mask.contains(block)) mask.add(block);
        return this;
    }
    public BlockMask without(String block)
    {
        if (block.startsWith("#"))
        {
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getCollection();
            ITag<net.minecraft.block.Block> tag = tags.get(new ResourceLocation(block.substring(1)));
            if (tag != null)
            {
                List<net.minecraft.block.Block> blocks = tag.getAllElements();
                for (net.minecraft.block.Block add : blocks) without(new Block(add.getDefaultState()));
            }
            return this;
        }
        else return without(KeystoneFilter.block(block));
    }
    public BlockMask without(Block block)
    {
        if (mask.contains(block)) mask.remove(block);
        return this;
    }
    public BlockMask blacklist()
    {
        this.blacklist = true;
        return this;
    }
    public BlockMask whitelist()
    {
        this.blacklist = false;
        return this;
    }

    public boolean valid(Block block) { return mask.contains(block) != blacklist; }
    public void forEach(Consumer<Block> consumer) { mask.forEach(block -> consumer.accept(block)); }
}
