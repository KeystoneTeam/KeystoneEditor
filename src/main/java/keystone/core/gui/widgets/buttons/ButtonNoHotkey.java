package keystone.core.gui.widgets.buttons;

import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ButtonNoHotkey extends ButtonWidget
{
    private IKeystoneTooltip tooltip;

    public ButtonNoHotkey(int x, int y, int width, int height, Text title, PressAction pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
    }
    public ButtonNoHotkey(int x, int y, int width, int height, Text title, PressAction pressedAction, IKeystoneTooltip tooltip)
    {
        super(x, y, width, height, title, pressedAction);
        this.tooltip = tooltip;
    }

    public void setTooltip(IKeystoneTooltip tooltip)
    {
        this.tooltip = tooltip;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY)
    {
        if (this.tooltip != null) KeystoneOverlayHandler.addTooltip(this.tooltip);
    }
}
