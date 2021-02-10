package keystone.core.gui.screens.filters;

import keystone.api.Keystone;
import keystone.api.filters.Variable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;

public class BooleanVariableWidget extends CheckboxButton
{
    private final FilterSelectionScreen parent;
    private final Field field;
    private final String name;

    public BooleanVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(x, y, width, getHeight(), new StringTextComponent(name), (boolean)field.get(parent.getFilterInstance()), true);

        this.parent = parent;
        this.field = field;
        this.name = name;
    }
    public static int getHeight() { return 20; }

    @Override
    public void onPress()
    {
        super.onPress();
        try
        {
            field.set(this.parent.getFilterInstance(), this.isChecked());
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            String error = "Cannot set filter variable '" + name + "'!";
            Keystone.LOGGER.error(error);
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
        }
    }
}
