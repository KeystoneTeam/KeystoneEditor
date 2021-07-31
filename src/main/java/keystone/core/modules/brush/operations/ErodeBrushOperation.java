package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.variables.Hook;
import keystone.api.variables.IntRange;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.Block;
import keystone.core.modules.WorldModifierModules;
import keystone.core.modules.brush.BrushOperation;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashMap;
import java.util.Map;

public class ErodeBrushOperation extends BrushOperation
{
    public enum Preset
    {
        MELT(2, 5, true, true),
        FILL(5, 2, true, true),
        SMOOTH(3, 3, true, true),
        LIFT(6, 1, false, true),
        LOWER(1, 6, true, false);

        private final int meltFaces;
        private final int fillFaces;
        private final boolean doMelt;
        private final boolean doFill;

        Preset(int meltFaces, int fillFaces, boolean doMelt, boolean doFill)
        {
            this.meltFaces = meltFaces;
            this.fillFaces = fillFaces;
            this.doMelt = doMelt;
            this.doFill = doFill;
        }
    }

    private static final Map<Block, Integer> neighborBlockCounts = new HashMap<>();

    @Variable("Preset") @Hook("onPresetChanged") Preset preset = Preset.SMOOTH;
    @Variable @IntRange(min = 1, max = 5) int strength = 1;
    @Variable @IntRange(min = 1, max = 6) int meltFaces;
    @Variable @IntRange(min = 1, max = 6) int fillFaces;
    @Variable boolean melt;
    @Variable boolean fill;

    @Override
    public ITextComponent getName()
    {
        return new TranslationTextComponent("keystone.brush.erode");
    }
    @Override
    public int iterations()
    {
        return strength * ((melt ? 1 : 0) + (fill ? 1 : 0));
    }
    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        if (iteration < strength * (melt ? 1 : 0)) meltIteration(x, y, z, worldModifiers);
        else fillIteration(x, y, z, worldModifiers);

        return true;
    }

    private void meltIteration(int x, int y, int z, WorldModifierModules worldModifiers)
    {
        Block currentBlock = worldModifiers.blocks.getBlock(x, y, z, RetrievalMode.LAST_SWAPPED);
        if (currentBlock.isAirOrLiquid()) return;

        neighborBlockCounts.clear();
        int highest = 1;
        Block highestBlock = currentBlock;
        int total = 0;

        for (Direction direction : Direction.values())
        {
            BlockPos neighborPos = new BlockPos(x, y, z).relative(direction);
            Block neighbor = worldModifiers.blocks.getBlock(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ(), RetrievalMode.LAST_SWAPPED);
            if (!neighbor.isAirOrLiquid()) continue;

            total++;
            Integer count = neighborBlockCounts.get(neighbor);
            if (count == null) count = 1;
            else count++;

            if (count > highest)
            {
                highest = count;
                highestBlock = neighbor;
            }

            neighborBlockCounts.put(neighbor, count);
        }

        if (total >= meltFaces) worldModifiers.blocks.setBlock(x, y, z, highestBlock);
    }
    private void fillIteration(int x, int y, int z, WorldModifierModules worldModifiers)
    {
        Block currentBlock = worldModifiers.blocks.getBlock(x, y, z, RetrievalMode.LAST_SWAPPED);
        if (!currentBlock.isAirOrLiquid()) return;

        neighborBlockCounts.clear();
        int highest = 1;
        Block highestBlock = currentBlock;
        int total = 0;

        for (Direction direction : Direction.values())
        {
            BlockPos neighborPos = new BlockPos(x, y, z).relative(direction);
            Block neighbor = worldModifiers.blocks.getBlock(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ(), RetrievalMode.LAST_SWAPPED);
            if (neighbor.isAirOrLiquid()) continue;

            total++;
            Integer count = neighborBlockCounts.get(neighbor);
            if (count == null) count = 1;
            else count++;

            if (count > highest)
            {
                highest = count;
                highestBlock = neighbor;
            }

            neighborBlockCounts.put(neighbor, count);
        }

        if (total >= meltFaces) worldModifiers.blocks.setBlock(x, y, z, highestBlock);
    }

    public void onPresetChanged()
    {
        meltFaces = preset.meltFaces;
        fillFaces = preset.fillFaces;
        melt = preset.doMelt;
        fill = preset.doFill;

        dirtyEditor();
    }
}