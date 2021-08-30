package keystone.core.registries;

import keystone.api.wrappers.blocks.BlockType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockTypeRegistry
{
    public static BlockType AIR;

    private static Map<BlockState, Short> idMap = new HashMap<>();
    private static BlockType[] registry;

    public static void buildRegistry()
    {
        short nextID = 1;
        List<BlockType> registryList = new ArrayList<>();
        registryList.add(null);

        for (Block block : ForgeRegistries.BLOCKS)
        {
            for (BlockState state : block.getStateDefinition().getPossibleStates())
            {
                idMap.put(state, nextID);
                registryList.add(new BlockType(nextID, state));
                nextID++;
            }
        }
        //registryList.sort(Comparator.comparing(BlockType::toString));
        registry = new BlockType[registryList.size()];
        registryList.toArray(registry);

        AIR = BlockTypeRegistry.fromMinecraftBlock(Blocks.AIR.defaultBlockState());
    }

    public static BlockType fromKeystoneID(short keystoneID)
    {
        return registry[keystoneID];
    }
    public static BlockType fromMinecraftBlock(BlockState state)
    {
        return registry[idMap.getOrDefault(state, (short)0)];
    }
    public static short getMinecraftKeystoneID(BlockState state)
    {
        return idMap.getOrDefault(state, (short)0);
    }
}
