package keystone.core.modules.brush;

import keystone.core.modules.brush.operations.*;
import keystone.core.modules.world.WorldModifierModules;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class BrushOperation
{
    public interface PositionValidator { boolean validate(int x, int y, int z); }

    public static final List<BrushOperation> VALUES = new ArrayList<>();
    private final int listIndex;

    public static final BrushOperation FILL = new FillBrushOperation();
    public static final BrushOperation ERODE = new ErodeBrushOperation();
    public static final BrushOperation GRAVITY = new GravityBrushOperation();
    public static final BrushOperation STACK_FILL = new StackFillBrushOperation();
    public static final BrushOperation SET_BIOME = new SetBiomeBrushOperation();

    protected BrushOperation()
    {
        listIndex = VALUES.size();
        VALUES.add(this);
    }

    public abstract Text getName();
    public int iterations() { return 1; }
    public abstract boolean process(int x, int y, int z, WorldModifierModules worldModifiers, int iteration);

    public final BrushOperation getNextOperation() { return VALUES.get((listIndex + 1) % VALUES.size()); }
}
