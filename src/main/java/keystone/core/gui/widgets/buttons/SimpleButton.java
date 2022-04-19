package keystone.core.gui.widgets.buttons;

import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SimpleButton extends ButtonNoHotkey
{
    private TextRenderer textRenderer;

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
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (active)
        {
            fill(matrixStack, x, y, x + width, y + height, borderColor);
            if (buttonColor != borderColor) fill(matrixStack, x + 1, y + 1, x + width - 1, y + height - 1, buttonColor);
            if (isHovered())
            {
                drawCenteredText(matrixStack, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColorHovered);
                renderTooltip(matrixStack, mouseX, mouseY);
            }
            else drawCenteredText(matrixStack, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
        }
        else
        {
            fill(matrixStack, x, y, x + width, y + height, borderColor);
            if (buttonColor != borderColor) fill(matrixStack, x + 1, y + 1, x + width - 1, y + height - 1, buttonColor);
            drawCenteredText(matrixStack, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColorDisabled);
            if (isHovered()) renderTooltip(matrixStack, mouseX, mouseY);
        }
    }
}
