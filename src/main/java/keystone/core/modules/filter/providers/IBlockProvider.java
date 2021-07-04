package keystone.core.modules.filter.providers;

import keystone.api.wrappers.blocks.Block;

public interface IBlockProvider
{
    Block get();
    Block getFirst();
    IBlockProvider clone();
}