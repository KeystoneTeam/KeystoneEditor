package keystone.core.gui.widgets.inputs;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public abstract class ParsableTextWidget<T> extends TextFieldWidget
{
    protected final MinecraftClient mc;
    protected final TextRenderer textRenderer;
    private T value;
    private String displayValue;
    
    private IKeystoneTooltip tooltip;
    private float tooltipDelay;
    private float tooltipTimer;
    private int tooltipX;
    private int tooltipY;
    
    public ParsableTextWidget(Text name, int x, int y, int width, T value)
    {
        super(MinecraftClient.getInstance().textRenderer, x, y, width, getFinalHeight(), name);

        this.mc = MinecraftClient.getInstance();
        this.textRenderer = mc.textRenderer;
        this.value = postProcess(value);
        this.displayValue = postProcessDisplay(this.value);
        this.tooltipDelay = KeystoneConfig.tooltipDelay;

        setMaxLength(256);
        setDrawsBackground(true);
        setText(this.displayValue);
    }
    public ParsableTextWidget<T> setTooltip(IKeystoneTooltip tooltip) { this.tooltip = tooltip; return this; }
    public ParsableTextWidget<T> setTooltipDelay(float delay) { this.tooltipDelay = delay; return this; }
    
    public static int getFieldOffset() { return 11; }
    public static int getFinalHeight() { return 23; }
    
    protected abstract T parse(String str) throws Exception;
    protected T postProcess(T value) { return value; }
    protected String postProcessDisplay(T value) { return value.toString(); }
    protected T reverseProcessDisplay(String displayValue) throws Exception { return parse(displayValue); }
    protected boolean onSetValue(T value) { return true; }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredText(matrixStack, textRenderer, getMessage(), x + width / 2, y, 0xFFFFFF);
        matrixStack.push();
        y += getFieldOffset();
        height -= getFieldOffset();
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        renderTooltip(matrixStack, mouseX, mouseY);
        height += getFieldOffset();
        y -= getFieldOffset();
        matrixStack.pop();
    }
    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY)
    {
        if (this.tooltip != null)
        {
            if (active && visible && hovered)
            {
                if (mouseX == tooltipX && mouseY == tooltipY) tooltipTimer += MinecraftClient.getInstance().getTickDelta();
                else if (tooltipTimer < tooltipDelay) tooltipTimer = 0;
                
                tooltipX = mouseX;
                tooltipY = mouseY;
                if (tooltipTimer >= tooltipDelay) KeystoneOverlayHandler.addTooltip(this.tooltip);
            }
            else tooltipTimer = 0;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (active) return super.mouseClicked(mouseX, mouseY, button);
        else return false;
    }

    @Override
    protected void setFocused(boolean focus)
    {
        if (focus != isFocused())
        {
            super.setFocused(focus);
            onFocusedChanged(focus);
        }
    }

    @Override
    protected void onFocusedChanged(boolean focused)
    {
        if (!focused)
        {
            try
            {
                T newValue = reverseProcessDisplay(getText());
                setTypedValue(newValue);
            }
            catch (Exception e)
            {
                String error = "Invalid value '" + getText() + "' for filter variable '" + getMessage().getString() + "'!";
                Keystone.LOGGER.error(error);
                mc.player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
            }
            finally
            {
                setText(displayValue);
            }
        }
        super.onFocusedChanged(focused);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!active || !isFocused()) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            setFocused(false);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public final T getTypedValue() { return value; }
    public final void setTypedValue(T newValue)
    {
        value = postProcess(newValue);
        displayValue = postProcessDisplay(value);
        if (onSetValue(newValue)) setText(displayValue);
    }
}
