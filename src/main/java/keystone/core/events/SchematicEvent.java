package keystone.core.events;

import keystone.api.wrappers.blocks.BlockType;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;

public class SchematicEvent extends Event
{
    public static class ScaleBlock extends SchematicEvent
    {
        public final BlockType blockType;
        public final int scale;
        public BlockType[] scaled;

        public ScaleBlock(BlockType blockType, int scale)
        {
            this.blockType = blockType;
            this.scale = scale;
            this.scaled = new BlockType[scale * scale * scale];
            Arrays.fill(this.scaled, blockType);
        }

        public void setBlock(int x, int y, int z, BlockType blockType)
        {
            scaled[index(x, y, z)] = blockType;
        }
        public BlockType getBlockType(int x, int y, int z)
        {
            return scaled[index(x, y, z)];
        }
        private int index(int x, int y, int z) { return z + y * scale + x * scale * scale; }
    }
}
