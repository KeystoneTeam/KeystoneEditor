package keystone.core.modules.world.submodules;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.client.Player;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.chunk.WorldHistoryChunk;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.RegistryLookups;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class BlocksModule implements IKeystoneModule
{
    public interface BlockListener
    {
        void onChanged(int x, int y, int z, BlockType blockType);
    }

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
        return worldCacheModule.getDimensionWorld(Player.getDimension()) != null;
    }

    public World getWorld()
    {
        return worldCacheModule.getDimensionWorld(Player.getDimension());
    }

    public BlockType getBlockType(int x, int y, int z, RetrievalMode retrievalMode)
    {
        if (!historyModule.isEntryOpen())
        {
            World world = worldCacheModule.getDimensionWorld(Player.getDimension());
            BlockPos pos = new BlockPos(x, y, z);
            return BlockTypeRegistry.fromMinecraftBlock(world.getBlockState(pos));
        }

        WorldHistoryChunk chunk = historyModule.getOpenEntry().getOrAddChunk(x, y, z);
        return chunk.getBlockType(x, y, z, retrievalMode);
    }
    public NBTCompound getBlockData(int x, int y, int z, RetrievalMode retrievalMode)
    {
        if (!historyModule.isEntryOpen())
        {
            World world = worldCacheModule.getDimensionWorld(Player.getDimension());
            BlockPos pos = new BlockPos(x, y, z);
            
            // Get World Chunk
            WorldChunk chunk = world.getWorldChunk(pos);
            if (chunk == null) return null;
            
            // Get Tile Entity from Chunk
            BlockEntity tileEntity = chunk.getBlockEntity(pos);
            if (tileEntity == null) return null;
            
            // Return wrapped block data
            return new NBTCompound(tileEntity.createNbtWithIdentifyingData(RegistryLookups.registryLookup()));
        }

        WorldHistoryChunk chunk = historyModule.getOpenEntry().getOrAddChunk(x, y, z);
        return chunk.getBlockData(x, y, z, retrievalMode);
    }
    
    /**
     * Set a {@link BlockType} in the current world. This will automatically hook into the history system, allowing
     * for undo and redo support. Be sure that the {@link keystone.core.modules.history.HistoryModule}
     * has an entry open first
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @param blockType The {@link BlockType} to set
     */
    public void setBlockType(int x, int y, int z, BlockType blockType)
    {
        historyModule.getOpenEntry().setBlockType(x, y, z, blockType);
    }
    public void setBlockData(int x, int y, int z, NBTCompound blockData)
    {
        historyModule.getOpenEntry().setBlockData(x, y, z, blockData);
    }

    public void swapBuffers(boolean copy)
    {
        historyModule.swapBlockBuffers(copy);
    }
}
