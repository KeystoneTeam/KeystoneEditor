package keystone.core.gui.widgets.buttons;

import keystone.core.gui.IKeystoneTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

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
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        context.drawTextWithShadow(textRenderer, getMessage(), getX(), getY(), color);
    }
}
