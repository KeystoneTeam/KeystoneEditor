package keystone.core.gui.widgets.buttons;

import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SimpleButton extends ButtonNoHotkey
{
    private final TextRenderer textRenderer;

    protected int borderColor = 0xFF404040;
    protected int buttonColor = 0xFF404040;
    protected int textColor = 0xFFFFFF00;
    protected int textColorHovered = 0xFFFFFFFF;
    protected int textColorDisabled = 0xFF808080;

    public SimpleButton(int x, int y, int width, int height, Text title, PressAction pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
    }
    public SimpleButton(int x, int y, int width, int height, Text title, PressAction pressedAction, IKeystoneTooltip tooltip)
    {
        super(x, y, width, height, title, pressedAction, tooltip);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
    }

    public SimpleButton setBorderColor(int borderColor)
    {
        this.borderColor = borderColor;
        return this;
    }
    public SimpleButton setButtonColor(int buttonColor)
    {
        this.buttonColor = buttonColor;
        return this;
    }
    public SimpleButton setFormatting(int textColor)
    {
        this.textColor = textColor;
        return this;
    }
    public SimpleButton setFormattingHovered(int textColorHovered)
    {
        this.textColorHovered = textColorHovered;
        return this;
    }
    public SimpleButton setFormattingDisabled(int textColorDisabled)
    {
        this.textColorDisabled = textColorDisabled;
        return this;
    }
    public SimpleButton setColors(int borderColor, int buttonColor, int textColor, int textColorHovered, int textColorDisabled)
    {
        this.borderColor = borderColor;
        this.buttonColor = buttonColor;
        this.textColor = textColor;
        this.textColorHovered = textColorHovered;
        this.textColorDisabled = textColorDisabled;
        return this;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        if (active)
        {
            context.fill(getX(), getY(), getX() + width, getY() + height, borderColor);
            if (buttonColor != borderColor) context.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, buttonColor);
            if (isSelected()) context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColorHovered);
            else context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
        }
        else
        {
            context.fill(getX(), getY(), getX() + width, getY() + height, borderColor);
            if (buttonColor != borderColor) context.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, buttonColor);
            context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColorDisabled);
        }
    
        renderTooltip(context, mouseX, mouseY);
    }
}
