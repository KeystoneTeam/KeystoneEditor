package keystone.core.gui.widgets.inputs.fields;

import keystone.api.variables.DisplayScale;
import keystone.api.variables.IntRange;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class IntegerFieldWidget extends ParsableTextFieldWidget<Integer>
{
    private final IntRange range;
    private final DisplayScale scale;

    public IntegerFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(screen, instance, field, name, x, y, width);
        this.range = field.getAnnotation(IntRange.class);
        this.scale = field.getAnnotation(DisplayScale.class);
        setTypedValue(getTypedValue());
    }

    @Override
    protected Integer parse(String str) throws Exception
    {
        return Integer.parseInt(str);
    }
    @Override
    protected Integer postProcess(Integer value)
    {
        if (this.range != null)
        {
            if (value < range.min()) value = range.min();
            if (value > range.max()) value = range.max();
        }
        return value;
    }
    @Override
    protected Integer postProcessDisplay(Integer value)
    {
        return (int)(scale != null ? scale.value() * value : value);
    }
    @Override
    protected Integer reverseProcessDisplay(Integer displayValue)
    {
        return scale != null ? (int)(displayValue / scale.value()) : displayValue;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered() && active && Screen.hasControlDown())
        {
            int step = range != null ? range.scrollStep() : 1;
            if (scale != null) step /= scale.value();

            if (delta > 0) setTypedValue(getTypedValue() + step);
            else if (delta < 0) setTypedValue(getTypedValue() - step);
            return true;
        }
        return false;
    }
}
