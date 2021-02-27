package keystone.core.gui.widgets.inputs.fields;

import keystone.api.filters.FloatRange;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class FloatFieldWidget extends ParsableTextFieldWidget<Float>
{
    private final FloatRange range;

    public FloatFieldWidget(Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(instance, field, name, x, y, width);
        this.range = field.getAnnotation(FloatRange.class);
    }

    @Override
    protected Float parse(String str) throws Exception
    {
        return Float.parseFloat(str);
    }

    @Override
    protected Float postProcess(Float value)
    {
        if (this.range != null)
        {
            if (value < range.min()) value = range.min();
            if (value > range.max()) value = range.max();
        }
        return value;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered())
        {
            float step = this.range != null ? this.range.scrollStep() : 1;
            setValue(getValue() + (float)delta * step);
            return true;
        }
        return false;
    }
}
