package keystone.gui.screens.filters;

import keystone.api.filters.FilterVariable;
import keystone.api.filters.IntRange;

import java.lang.reflect.Field;

public class IntegerVariableWidget extends AbstractTextVariableWidget<Integer>
{
    private final IntRange range;

    public IntegerVariableWidget(FilterSelectionScreen parent, FilterVariable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(parent, variable, field, name, x, y, width);
        this.range = field.getAnnotation(IntRange.class);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered())
        {
            if (delta > 0) setValue(getValue() + 1);
            else if (delta < 0) setValue(getValue() - 1);
            return true;
        }
        return false;
    }
}
