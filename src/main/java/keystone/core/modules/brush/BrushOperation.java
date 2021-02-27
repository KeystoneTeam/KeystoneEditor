package keystone.core.modules.brush;

import keystone.api.filters.Variable;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class BrushOperation
{
    public static final List<BrushOperation> VALUES = new ArrayList<>();
    private final int listIndex;

    public static final BrushOperation FILL = new BrushOperation()
    {
        @Variable BlockMask mask = new BlockMask().with("minecraft:air");
        @Variable BlockPalette palette = new BlockPalette().with("minecraft:stone");
        @Variable boolean useMask = false;

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

    protected BrushOperation()
    {
        listIndex = VALUES.size();
        VALUES.add(this);
    }

    public void prepare(World world)
    {
        this.world = world;
    }
    public abstract ITextComponent getName();
    public abstract Block process(BlockPos pos);

    public final BrushOperation getNextOperation() { return VALUES.get((listIndex + 1) % VALUES.size()); }

    protected final Block getBlock(BlockPos pos)
    {
        return new Block(world.getBlockState(pos), world.getTileEntity(pos));
    }
}
