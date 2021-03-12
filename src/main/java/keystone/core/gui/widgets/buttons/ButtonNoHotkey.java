package keystone.core.gui.widgets.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class ButtonNoHotkey extends Button
{
    private IKeystoneTooltip tooltip;

    public ButtonNoHotkey(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
    }
    public ButtonNoHotkey(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, IKeystoneTooltip tooltip)
    {
        super(x, y, width, height, title, pressedAction);
        this.tooltip = tooltip;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }
    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        if (this.tooltip != null) KeystoneOverlayHandler.addTooltip(this.tooltip);
    }
}
