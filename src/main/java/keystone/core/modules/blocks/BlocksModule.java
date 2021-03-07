package keystone.core.modules.blocks;

import keystone.api.Keystone;
import keystone.api.enums.BlockRetrievalMode;
import keystone.api.wrappers.Block;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.BlockHistoryChunk;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlocksModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private WorldCacheModule worldCacheModule;

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        worldCacheModule = Keystone.getModule(WorldCacheModule.class);
    }
    @Override
    public boolean isEnabled()
    {
        return worldCacheModule.getDimensionWorld(Player.getDimensionId()) != null;
    }

    public World getWorld()
    {
        return worldCacheModule.getDimensionWorld(Player.getDimensionId());
    }

    /**
     * Set a {@link keystone.api.wrappers.Block} in the current world. This will automatically hook into the history system, allowing
     * for undo and redo support. Be sure that the {@link keystone.core.modules.history.HistoryModule}
     * has an entry open first
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @param block The {@link keystone.api.wrappers.Block} to set
     */
    public void setBlock(int x, int y, int z, Block block)
    {
        historyModule.getOpenEntry().setBlock(x, y, z, block);
    }
    public Block getBlock(int x, int y, int z, BlockRetrievalMode retrievalMode)
    {
        World world = worldCacheModule.getDimensionWorld(Player.getDimensionId());
        if (!historyModule.isEntryOpen())
        {
            BlockPos pos = new BlockPos(x, y, z);
            return new Block(world.getBlockState(pos), world.getTileEntity(pos));
        }

        BlockHistoryChunk chunk = historyModule.getOpenEntry().getChunk(x, y, z);
        if (chunk != null) return chunk.getBlock(x, y, z, retrievalMode);
        else
        {
            BlockPos pos = new BlockPos(x, y, z);
            return new Block(world.getBlockState(pos), world.getTileEntity(pos));
        }
    }

    public void swapBuffers(boolean copy)
    {
        historyModule.swapBlockBuffers(copy);
    }
}
