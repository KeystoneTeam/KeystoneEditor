package keystone.core.filters.providers;

import keystone.api.wrappers.Block;

public interface IBlockProvider
{
    Block get();
    Block getFirst();
    IBlockProvider clone();
}