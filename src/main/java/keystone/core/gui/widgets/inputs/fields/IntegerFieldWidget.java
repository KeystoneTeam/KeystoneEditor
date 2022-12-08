package keystone.core.gui.widgets.inputs.fields;

import keystone.api.variables.DisplayModifiers;
import keystone.api.variables.IntRange;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.function.Supplier;

public class IntegerFieldWidget extends ParsableTextFieldWidget<Integer>
{
    private final IntRange range;
    private final DisplayModifiers displayModifiers;

    public IntegerFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(screen, instance, field, name, x, y, width);
        this.range = field.getAnnotation(IntRange.class);
        this.displayModifiers = field.getAnnotation(DisplayModifiers.class);
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
    protected String postProcessDisplay(Integer value)
    {
        if (displayModifiers != null)
        {
            float scaled = displayModifiers.numberScale() * value;
            if (displayModifiers.decimalPoints() >= 0)
            {
                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.setMaximumFractionDigits(displayModifiers.decimalPoints());
                return decimalFormat.format(scaled);
            }
            else return Float.toString(scaled);
        }
        else return value.toString();
    }
    @Override
    protected Integer reverseProcessDisplay(String displayValue)
    {
        float parsed = Float.parseFloat(displayValue);
        return displayModifiers != null ? (int)(parsed / displayModifiers.numberScale()) : (int)parsed;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered() && active && Screen.hasControlDown())
        {
            int step = range != null ? range.scrollStep() : 1;
            if (displayModifiers != null) step /= displayModifiers.numberScale();

            if (delta > 0) setTypedValue(getTypedValue() + step);
            else if (delta < 0) setTypedValue(getTypedValue() - step);
            return true;
        }
        return false;
    }
}
