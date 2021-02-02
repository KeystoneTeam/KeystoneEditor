package keystone.gui.screens.filters;

import keystone.api.filters.Variable;

import java.lang.reflect.Field;

public class StringVariableWidget extends AbstractTextVariableWidget<String>
{
    public StringVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(parent, variable, field, name, x, y, width);
    }

    @Override
    protected String parse(String str) throws Exception
    {
        return str;
    }
}
