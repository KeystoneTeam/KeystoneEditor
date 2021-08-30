package keystone.core.modules.filter.providers;

import keystone.api.wrappers.blocks.BlockType;

public interface IBlockProvider
{
    BlockType get();
    BlockType getFirst();
    IBlockProvider clone();
}