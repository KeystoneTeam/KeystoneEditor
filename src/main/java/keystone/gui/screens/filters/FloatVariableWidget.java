package keystone.gui.screens.filters;

import keystone.api.filters.Variable;
import keystone.api.filters.FloatRange;

import java.lang.reflect.Field;

public class FloatVariableWidget extends AbstractTextVariableWidget<Float>
{
    private final FloatRange range;

    public FloatVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
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
            float step = this.range != null ? this.range.scrollStep() : 1;
            setValue(getValue() + (float)delta * step);
            return true;
        }
        return false;
    }
}
