package keystone.core.gui.screens.filters;

import keystone.api.filters.Variable;
import keystone.api.filters.IntRange;

import java.lang.reflect.Field;

public class IntegerVariableWidget extends AbstractTextVariableWidget<Integer>
{
    private final IntRange range;

    public IntegerVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
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
            int step = this.range != null ? this.range.scrollStep() : 1;

            if (delta > 0) setValue(getValue() + step);
            else if (delta < 0) setValue(getValue() - step);
            return true;
        }
        return false;
    }
}
