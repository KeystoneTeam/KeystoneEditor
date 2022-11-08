package keystone.core.gui.widgets.inputs.fields;

import keystone.api.variables.IntRange;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class IntegerFieldWidget extends ParsableTextFieldWidget<Integer>
{
    private final IntRange range;

    public IntegerFieldWidget(Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(instance, field, name, x, y, width);
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
        if (isHovered() && active && Screen.hasControlDown())
        {
            int step = this.range != null ? this.range.scrollStep() : 1;

            if (delta > 0) setTypedValue(getTypedValue() + step);
            else if (delta < 0) setTypedValue(getTypedValue() - step);
            return true;
        }
        return false;
    }
}
