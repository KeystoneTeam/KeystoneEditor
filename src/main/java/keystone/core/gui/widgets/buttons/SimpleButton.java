package keystone.core.gui.widgets.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

public class SimpleButton extends ButtonNoHotkey
{
    private FontRenderer font;

    protected int buttonColor = 0xFF404040;
    protected int textColor = 0xFFFFFF00;
    protected int textColorHovered = 0xFFFFFFFF;
    protected int textColorDisabled = 0xFF808080;

    public SimpleButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
        this.font = Minecraft.getInstance().fontRenderer;
    }
    public SimpleButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, ITooltip onTooltip)
    {
        super(x, y, width, height, title, pressedAction, onTooltip);
        this.font = Minecraft.getInstance().fontRenderer;
    }

    public SimpleButton setColors(int buttonColor, int textColor, int textColorHovered, int textColorDisabled)
    {
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
            fill(matrixStack, x, y, x + width, y + height, buttonColor);
            if (isHovered())
            {
                drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColorHovered);
                renderToolTip(matrixStack, mouseX, mouseY);
            }
            else drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
        }
        else
        {
            fill(matrixStack, x, y, x + width, y + height, 0xFF404040);
            drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColorDisabled);
            if (isHovered()) renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
