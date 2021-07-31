package keystone.core.gui.widgets.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

public class SimpleButton extends ButtonNoHotkey
{
    private FontRenderer font;

    protected int borderColor = 0xFF404040;
    protected int buttonColor = 0xFF404040;
    protected int textColor = 0xFFFFFF00;
    protected int textColorHovered = 0xFFFFFFFF;
    protected int textColorDisabled = 0xFF808080;

    public SimpleButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
        this.font = Minecraft.getInstance().font;
    }
    public SimpleButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, IKeystoneTooltip tooltip)
    {
        super(x, y, width, height, title, pressedAction, tooltip);
        this.font = Minecraft.getInstance().font;
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
    public SimpleButton setTextColor(int textColor)
    {
        this.textColor = textColor;
        return this;
    }
    public SimpleButton setTextColorHovered(int textColorHovered)
    {
        this.textColorHovered = textColorHovered;
        return this;
    }
    public SimpleButton setTextColorDisabled(int textColorDisabled)
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
                drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColorHovered);
                renderToolTip(matrixStack, mouseX, mouseY);
            }
            else drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
        }
        else
        {
            fill(matrixStack, x, y, x + width, y + height, borderColor);
            if (buttonColor != borderColor) fill(matrixStack, x + 1, y + 1, x + width - 1, y + height - 1, buttonColor);
            drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColorDisabled);
            if (isHovered()) renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
