package keystone.core.gui.widgets.inputs;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

public abstract class ParsableTextWidget<T> extends TextFieldWidget
{
    protected final Minecraft mc;
    protected final FontRenderer font;
    private T value;

    public ParsableTextWidget(ITextComponent name, int x, int y, int width, T value)
    {
        super(Minecraft.getInstance().font, x, y, width, getFinalHeight(), name);

        this.mc = Minecraft.getInstance();
        this.font = mc.font;
        this.value = postProcess(value);

        setMaxLength(256);
        setBordered(true);
        setValue(this.value.toString());
    }
    public static int getFieldOffset() { return 11; }
    public static int getFinalHeight() { return 23; }

    protected abstract T parse(String str) throws Exception;
    protected T postProcess(T value) { return value; }
    protected boolean onSetValue(T value) { return true; }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, getMessage(), x + width / 2, y, 0xFFFFFF);
        matrixStack.pushPose();
        y += getFieldOffset();
        height -= getFieldOffset();
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        height += getFieldOffset();
        y -= getFieldOffset();
        matrixStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (active) return super.mouseClicked(mouseX, mouseY, button);
        else return false;
    }


    @Override
    public void setFocus(boolean isFocusedIn)
    {
        if (!isFocusedIn && isFocused())
        {
            try
            {
                T parsed = parse(getValue());
                setTypedValue(parsed);
            }
            catch (Exception e)
            {
                String error = "Invalid value '" + getValue() + "' for filter variable '" + getMessage().getString() + "'!";
                Keystone.LOGGER.error(error);
                mc.player.sendMessage(new StringTextComponent(error).withStyle(TextFormatting.RED), Util.NIL_UUID);
            }
            finally
            {
                setValue(value.toString());
                super.setFocus(isFocusedIn);
                return;
            }
        }
        else super.setFocus(isFocusedIn);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!active) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            setFocus(false);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public final T getTypedValue() { return value; }
    public final void setTypedValue(T newValue)
    {
        value = postProcess(newValue);
        if (onSetValue(newValue)) setValue(value.toString());
    }
}
