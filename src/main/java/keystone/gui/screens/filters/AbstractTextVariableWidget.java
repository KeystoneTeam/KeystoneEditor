package keystone.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.filters.FilterVariable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public abstract class AbstractTextVariableWidget<T> extends TextFieldWidget
{
    protected final Minecraft mc;
    protected final FontRenderer font;
    protected final FilterSelectionScreen parent;
    protected final FilterVariable variable;
    protected final Field field;
    protected final String name;
    private T value;

    public AbstractTextVariableWidget(FilterSelectionScreen parent, FilterVariable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(Minecraft.getInstance().fontRenderer, x, y + 11, width, getHeight() - 11, new StringTextComponent(name));

        this.mc = Minecraft.getInstance();
        this.font = mc.fontRenderer;
        this.parent = parent;
        this.variable = variable;
        this.field = field;
        this.name = name;
        this.value = postProcess((T)field.get(parent.getFilterInstance()));

        setMaxStringLength(256);
        setEnableBackgroundDrawing(true);
        setText(this.value.toString());
    }
    public static int getHeight() { return 23; }

    protected abstract T parse(String str) throws Exception;
    protected T postProcess(T value) { return value; }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, name, x + width / 2, y - 11, 0xFFFFFF);
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }
    @Override
    public void setFocused2(boolean isFocusedIn)
    {
        if (!isFocusedIn && isFocused())
        {
            try
            {
                T parsed = parse(getText());
                setValue(parsed);
            }
            catch (Exception e)
            {
                String error = "Invalid value '" + getText() + "' for filter variable '" + name + "'!";
                Keystone.LOGGER.error(error);
                mc.player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
            }
            finally
            {
                setText(value.toString());
                super.setFocused2(isFocusedIn);
                return;
            }
        }
        else super.setFocused2(isFocusedIn);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            setFocused2(false);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected T getValue() { return value; }
    protected void setValue(T newValue)
    {
        try
        {
            value = postProcess(newValue);
            field.set(parent.getFilterInstance(), value);
            setText(value.toString());
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set filter variable '" + name + "'!";
            Keystone.LOGGER.error(error);
            mc.player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
        }
    }
}
