package keystone.gui.screens.filters;

import keystone.api.filters.FilterVariable;
import keystone.api.filters.FloatRange;

import java.lang.reflect.Field;

public class FloatVariableWidget extends AbstractTextVariableWidget<Float>
{
    private final FloatRange range;

    public FloatVariableWidget(FilterSelectionScreen parent, FilterVariable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(parent, variable, field, name, x, y, width);
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
            setValue(getValue() + (float)delta);
            return true;
        }
        return false;
    }
}
