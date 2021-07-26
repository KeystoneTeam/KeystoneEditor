package keystone.core.events;

import keystone.api.wrappers.blocks.Block;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;

public class SchematicEvent extends Event
{
    public static class ScaleBlock extends SchematicEvent
    {
        public final Block block;
        public final int scale;
        public Block[] scaled;

        public ScaleBlock(Block block, int scale)
        {
            this.block = block;
            this.scale = scale;
            this.scaled = new Block[scale * scale * scale];
            Arrays.fill(this.scaled, block);
        }

        public void setBlock(int x, int y, int z, Block block)
        {
            scaled[index(x, y, z)] = block;
        }
        public Block getBlock(int x, int y, int z)
        {
            return scaled[index(x, y, z)];
        }
        private int index(int x, int y, int z) { return z + y * scale + x * scale * scale; }
    }
}
