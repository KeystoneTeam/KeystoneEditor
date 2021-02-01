package keystone.core.utils;

import keystone.api.Keystone;
import keystone.gui.screens.block_selection.AbstractBlockSelectionScreen;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

public class BlockUtils
{
    private static final Map<String, String> blockToItemMap = new HashMap<>();
    static
    {
        blockToItemMap.put("minecraft:air", "minecraft:barrier");
        blockToItemMap.put("minecraft:water", "minecraft:water_bucket");
        blockToItemMap.put("minecraft:lava", "minecraft:lava_bucket");
    }

    public static final Item getBlockItem(Block block, IForgeRegistry<Item> itemRegistry)
    {
        Item item = block.asItem();
        if (blockToItemMap.containsKey(block.getRegistryName().toString())) item = itemRegistry.getValue(new ResourceLocation(blockToItemMap.get(block.getRegistryName().toString())));

        if (item == null || item == Items.AIR)
        {
            if (AbstractBlockSelectionScreen.DEBUG_LOG) Keystone.LOGGER.info("No item for block " + block.getRegistryName().toString());
            return null;
        }
        else return item;
    }
}
