package keystone.core.gui.widgets.inputs.fields;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class StringFieldWidget extends ParsableTextFieldWidget<String>
{
    public StringFieldWidget(Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(instance, field, name, x, y, width);
    }

    @Override
    protected String parse(String str) throws Exception
    {
        return str;
    }
}
