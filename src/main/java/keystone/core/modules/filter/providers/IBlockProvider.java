package keystone.core.modules.filter.providers;

import keystone.api.wrappers.Block;

public interface IBlockProvider
{
    Block get();
    Block getFirst();
    IBlockProvider clone();
}