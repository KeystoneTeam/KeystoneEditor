package keystone.core.utils;

import keystone.api.Keystone;
import keystone.core.gui.overlays.block_selection.AbstractBlockSelectionScreen;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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

    public static Item getBlockItem(Block block)
    {
        Item item = block.asItem();
        String blockIdentifier = Registries.BLOCK.getId(block).toString();
        if (blockToItemMap.containsKey(blockIdentifier)) item = Registries.ITEM.get(new Identifier(blockToItemMap.get(blockIdentifier)));

        if (item == null || item == Items.AIR)
        {
            if (AbstractBlockSelectionScreen.DEBUG_LOG) Keystone.LOGGER.info("No item for block " + blockIdentifier);
            return Items.BARRIER;
        }
        else return item;
    }
}
