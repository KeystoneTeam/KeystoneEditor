package keystone.core.gui.widgets.inputs.fields;

import keystone.api.variables.DisplayModifiers;
import keystone.api.variables.FloatRange;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.function.Supplier;

public class FloatFieldWidget extends ParsableTextFieldWidget<Float>
{
    private final FloatRange range;
    private final DisplayModifiers displayModifiers;

    public FloatFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(screen, instance, field, name, x, y, width);
        this.range = field.getAnnotation(FloatRange.class);
        this.displayModifiers = field.getAnnotation(DisplayModifiers.class);
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
    protected String postProcessDisplay(Float value)
    {
        if (displayModifiers != null)
        {
            float scaled = displayModifiers.numberScale() * value;
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setMinimumFractionDigits(1);
            if (displayModifiers.decimalPoints() >= 0) decimalFormat.setMaximumFractionDigits(displayModifiers.decimalPoints());
            return decimalFormat.format(scaled);
        }
        else return value.toString();
    }
    @Override
    protected Float reverseProcessDisplay(String displayValue)
    {
        float parsed = Float.parseFloat(displayValue);
        return displayModifiers != null ? parsed / displayModifiers.numberScale() : parsed;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered() && active && Screen.hasControlDown())
        {
            float step = range != null ? range.scrollStep() : 1;
            if (displayModifiers != null) step /= displayModifiers.numberScale();
            setTypedValue(getTypedValue() + (float)delta * step);
            return true;
        }
        return false;
    }
}
