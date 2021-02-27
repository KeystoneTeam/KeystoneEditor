package keystone.core.gui.widgets.inputs;

import net.minecraft.util.text.ITextComponent;

public class IntegerWidget extends ParsableTextWidget<Integer>
{
    private int min;
    private int max;
    private int step;

    public IntegerWidget(ITextComponent name, int x, int y, int width, int value) { this(name, x, y, width, value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1); }
    public IntegerWidget(ITextComponent name, int x, int y, int width, int value, int min, int max) { this(name, x, y, width, value, min, max, 1); }
    public IntegerWidget(ITextComponent name, int x, int y, int width, int value, int min, int max, int step)
    {
        super(name, x, y, width, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    protected Integer parse(String str) throws Exception
    {
        return Integer.parseInt(str);
    }
    @Override
    protected Integer postProcess(Integer value)
    {
        if (step == 0) return value;

        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered())
        {
            if (delta > 0) setValue(getValue() + step);
            else if (delta < 0) setValue(getValue() - step);
            return true;
        }
        return false;
    }
}
