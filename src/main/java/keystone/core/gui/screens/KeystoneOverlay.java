package keystone.core.gui.screens;

import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class KeystoneOverlay extends Screen
{
    protected KeystoneOverlay(ITextComponent titleIn)
    {
        super(titleIn);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    @Override
    public void closeScreen()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }
    public void checkMouseOverGui()
    {
        this.buttons.forEach(widget -> { if (widget.isHovered() && widget.visible && widget.active) KeystoneOverlayHandler.MouseOverGUI = true; });
    }
}
