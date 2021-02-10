package keystone.core.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class SimpleButton extends ButtonNoHotkey
{
    private FontRenderer font;

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

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (active)
        {
            fill(matrixStack, x, y, x + width, y + height, 0xFF404040);
            if (isHovered())
            {
                drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFFFF);
                renderToolTip(matrixStack, mouseX, mouseY);
            }
            else drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFF00);
        }
        else
        {
            fill(matrixStack, x, y, x + width, y + height, 0xFF404040);
            drawCenteredString(matrixStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFF808080);
            if (isHovered()) renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
