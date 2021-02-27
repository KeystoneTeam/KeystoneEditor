package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class BooleanFieldWidget extends CheckboxButton
{
    private final Supplier<Object> instance;
    private final Field field;
    private final String name;

    public BooleanFieldWidget(Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(x, y, width, getHeight(), new StringTextComponent(name), (boolean)field.get(instance.get()), true);

        this.instance = instance;
        this.field = field;
        this.name = name;
    }
    public static int getHeight() { return 20; }

    @Override
    public int getHeightRealms()
    {
        return getHeight();
    }
    @Override
    public void onPress()
    {
        super.onPress();
        try
        {
            field.set(instance.get(), this.isChecked());
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            String error = "Cannot set Boolean field '" + name + "'!";
            Keystone.LOGGER.error(error);
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
        }
    }
}
