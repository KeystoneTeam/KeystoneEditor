package keystone.core.gui.widgets.inputs;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class FloatWidget extends ParsableTextWidget<Float>
{
    protected float min;
    protected float max;
    protected float step;

    public FloatWidget(Text name, int x, int y, int width, float value) { this(name, x, y, width, value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1); }
    public FloatWidget(Text name, int x, int y, int width, float value, float min, float max) { this(name, x, y, width, value, min, max, 1); }
    public FloatWidget(Text name, int x, int y, int width, float value, float min, float max, float step)
    {
        super(name, x, y, width, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    protected Float parse(String str) throws Exception
    {
        return Float.parseFloat(str);
    }
    @Override
    protected Float postProcess(Float value)
    {
        if (step == 0) return value;

        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (isSelected() && active && Screen.hasControlDown())
        {
            if (verticalAmount > 0) setTypedValue(getTypedValue() + step);
            else if (verticalAmount < 0) setTypedValue(getTypedValue() - step);
            return true;
        }
        return false;
    }
}
