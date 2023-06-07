package keystone.core.modules.brush.operations;

import keystone.api.enums.RetrievalMode;
import keystone.api.variables.Hide;
import keystone.api.variables.Hook;
import keystone.api.variables.IntRange;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.modules.brush.BrushOperation;
import keystone.core.modules.world.WorldModifierModules;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
        LOWER(1, 6, true, false),
        @Hide CUSTOM(0, 0, false, false);

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

    private static final Map<BlockType, Integer> neighborBlockCounts = new HashMap<>();

    @Tooltip("Select an erosion preset to use instead of custom values.")
    @Variable("Preset") @Hook("onPresetChanged") Preset preset = Preset.SMOOTH;
    
    @Tooltip("The number of times to perform the erosion operation.")
    @Variable @IntRange(min = 1, max = 5) @Hook("onSettingsChanged") int strength = 1;
    
    @Tooltip("The minimum blocks surrounding a block to perform the erosion when melting.")
    @Variable @IntRange(min = 1, max = 6) @Hook("onSettingsChanged") int meltFaces;
    
    @Tooltip("The minimum blocks surrounding a block to perform the erosion when filling.")
    @Variable @IntRange(min = 1, max = 6) @Hook("onSettingsChanged") int fillFaces;
    
    @Tooltip("Whether to perform melting. If false, blocks will not be removed.")
    @Variable @Hook("onSettingsChanged") boolean melt;
    
    @Tooltip("Whether to perform filling. If false, blocks placed.")
    @Variable @Hook("onSettingsChanged") boolean fill;

    @Override
    public Text getName()
    {
        return Text.translatable("keystone.brush.erode");
    }
    @Override
    public int iterations()
    {
        return strength * ((melt ? 1 : 0) + (fill ? 1 : 0));
    }
    @Override
    public boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration)
    {
        if (melt)
        {
            if (fill && iteration % 2 == 1) fillIteration(x, y, z, worldModifiers);
            else meltIteration(x, y, z, worldModifiers);
        }
        else fillIteration(x, y, z, worldModifiers);
        
        //if (iteration < strength * (melt ? 1 : 0)) meltIteration(x, y, z, worldModifiers);
        //else fillIteration(x, y, z, worldModifiers);

        return true;
    }

    private void meltIteration(int x, int y, int z, WorldModifierModules worldModifiers)
    {
        BlockType currentBlockType = worldModifiers.blocks.getBlockType(x, y, z, RetrievalMode.LAST_SWAPPED);
        if (currentBlockType.isAirOrLiquid()) return;

        neighborBlockCounts.clear();
        int highest = 1;
        BlockType highestBlockType = currentBlockType;
        int total = 0;

        for (Direction direction : Direction.values())
        {
            BlockPos neighborPos = new BlockPos(x, y, z).offset(direction);
            BlockType neighbor = worldModifiers.blocks.getBlockType(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ(), RetrievalMode.LAST_SWAPPED);
            if (!neighbor.isAirOrLiquid()) continue;

            total++;
            Integer count = neighborBlockCounts.get(neighbor);
            if (count == null) count = 1;
            else count++;

            if (count > highest)
            {
                highest = count;
                highestBlockType = neighbor;
            }

            neighborBlockCounts.put(neighbor, count);
        }

        if (total >= meltFaces) worldModifiers.blocks.setBlock(x, y, z, highestBlockType);
    }
    private void fillIteration(int x, int y, int z, WorldModifierModules worldModifiers)
    {
        BlockType currentBlockType = worldModifiers.blocks.getBlockType(x, y, z, RetrievalMode.LAST_SWAPPED);
        if (!currentBlockType.isAirOrLiquid()) return;

        neighborBlockCounts.clear();
        int highest = 1;
        BlockType highestBlockType = currentBlockType;
        int total = 0;

        for (Direction direction : Direction.values())
        {
            BlockPos neighborPos = new BlockPos(x, y, z).offset(direction);
            BlockType neighbor = worldModifiers.blocks.getBlockType(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ(), RetrievalMode.LAST_SWAPPED);
            if (neighbor.isAirOrLiquid()) continue;

            total++;
            Integer count = neighborBlockCounts.get(neighbor);
            if (count == null) count = 1;
            else count++;

            if (count > highest)
            {
                highest = count;
                highestBlockType = neighbor;
            }

            neighborBlockCounts.put(neighbor, count);
        }

        if (total >= meltFaces) worldModifiers.blocks.setBlock(x, y, z, highestBlockType);
    }

    public void onPresetChanged()
    {
        if (preset != Preset.CUSTOM)
        {
            meltFaces = preset.meltFaces;
            fillFaces = preset.fillFaces;
            melt = preset.doMelt;
            fill = preset.doFill;
            dirtyEditor();
        }
    }
    public void onSettingsChanged()
    {
        preset = Preset.CUSTOM;
        for (Preset test : Preset.values()) if (meltFaces == test.meltFaces && fillFaces == test.fillFaces && melt == test.doMelt && fill == test.doFill) preset = test;
        dirtyEditor();
    }
}