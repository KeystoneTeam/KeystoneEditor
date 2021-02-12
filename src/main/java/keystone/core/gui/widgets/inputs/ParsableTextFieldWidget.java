package keystone.core.gui.widgets.inputs;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.filters.Variable;
import keystone.core.gui.screens.filters.FilterSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public abstract class ParsableTextFieldWidget<T> extends TextFieldWidget
{
    protected final Minecraft mc;
    protected final FontRenderer font;
    private T value;

    public ParsableTextFieldWidget(ITextComponent name, int x, int y, int width, T value)
    {
        super(Minecraft.getInstance().fontRenderer, x, y + 11, width, getHeight() - 11, name);

        this.mc = Minecraft.getInstance();
        this.font = mc.fontRenderer;
        this.value = postProcess(value);

        setMaxStringLength(256);
        setEnableBackgroundDrawing(true);
        setText(this.value.toString());
    }
    public static int getHeight() { return 23; }

    protected abstract T parse(String str) throws Exception;
    protected T postProcess(T value) { return value; }
    protected boolean onSetValue(T value) { return true; }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, getMessage(), x + width / 2, y - 11, 0xFFFFFF);
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (active) return super.mouseClicked(mouseX, mouseY, button);
        else return false;
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
                String error = "Invalid value '" + getText() + "' for filter variable '" + getMessage().getString() + "'!";
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
        if (!active) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            setFocused2(false);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected final T getValue() { return value; }
    protected final void setValue(T newValue)
    {
        value = postProcess(newValue);
        if (onSetValue(newValue)) setText(value.toString());
    }
}
