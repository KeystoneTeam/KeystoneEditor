package keystone.core.modules.brush;

import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public abstract class BrushOperation
{
    public static final BrushOperation FILL = new BrushOperation()
    {
        private BlockMask mask = new BlockMask().with("minecraft:air");
        private BlockPalette palette = new BlockPalette().with("minecraft:stone");
        private boolean useMask = false;

        @Override
        public ITextComponent getName()
        {
            return new TranslationTextComponent("keystone.brush.fill");
        }
        @Override
        public Block process(BlockPos pos)
        {
            Block existing = getBlock(pos);
            if (!useMask || mask.valid(existing)) return palette.randomBlock();
            else return existing;
        }
    };

    private World world;

    public void prepare(World world)
    {
        this.world = world;
    }
    public abstract ITextComponent getName();
    public abstract Block process(BlockPos pos);

    protected final Block getBlock(BlockPos pos)
    {
        return new Block(world.getBlockState(pos), world.getTileEntity(pos));
    }
}
