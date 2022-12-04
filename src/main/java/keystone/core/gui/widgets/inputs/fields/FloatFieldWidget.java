package keystone.core.gui.widgets.inputs.fields;

import keystone.api.variables.DisplayScale;
import keystone.api.variables.FloatRange;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class FloatFieldWidget extends ParsableTextFieldWidget<Float>
{
    private final FloatRange range;
    private final DisplayScale scale;

    public FloatFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(screen, instance, field, name, x, y, width);
        this.range = field.getAnnotation(FloatRange.class);
        this.scale = field.getAnnotation(DisplayScale.class);
        setTypedValue(getTypedValue());
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
    protected Float postProcessDisplay(Float value)
    {
        return scale != null ? scale.value() * value : value;
    }
    
    @Override
    protected Float reverseProcessDisplay(Float displayValue)
    {
        return scale != null ? displayValue / scale.value() : displayValue;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered() && active && Screen.hasControlDown())
        {
            float step = range != null ? range.scrollStep() : 1;
            if (scale != null) step /= scale.value();
            setTypedValue(getTypedValue() + (float)delta * step);
            return true;
        }
        return false;
    }
}
