package keystone.core.gui.widgets.buttons;

import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class TextClickButton extends ButtonNoHotkey
{
    private TextRenderer textRenderer;
    private int color;

    public TextClickButton(int x, int y, Text text, int color, PressAction pressedAction)
    {
        this(x, y, text, color, pressedAction, null);
    }
    public TextClickButton(int x, int y, Text text, int color, PressAction pressedAction, IKeystoneTooltip tooltip)
    {
        super(x, y, MinecraftClient.getInstance().textRenderer.getWidth(text), 10, text, pressedAction, tooltip);
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.color = color;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        drawTextWithShadow(stack, textRenderer, getMessage(), x, y, color);
    }
}
