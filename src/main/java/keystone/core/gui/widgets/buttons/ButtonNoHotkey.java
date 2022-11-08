package keystone.core.gui.widgets.buttons;

import keystone.core.KeystoneConfig;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ButtonNoHotkey extends ButtonWidget
{
    private IKeystoneTooltip tooltip;
    private float tooltipDelay;
    private float tooltipTimer;
    private int tooltipX;
    private int tooltipY;

    public ButtonNoHotkey(int x, int y, int width, int height, Text title, PressAction pressedAction)
    {
        this(x, y, width, height, title, pressedAction, null);
    }
    public ButtonNoHotkey(int x, int y, int width, int height, Text title, PressAction pressedAction, IKeystoneTooltip tooltip)
    {
        super(x, y, width, height, title, pressedAction);
        this.tooltip = tooltip;
        this.tooltipDelay = KeystoneConfig.tooltipDelay;
    }

    public ButtonNoHotkey setTooltip(IKeystoneTooltip tooltip) { this.tooltip = tooltip; return this; }
    public ButtonNoHotkey setTooltipDelay(float delay) { this.tooltipDelay = delay; return this; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }
    
    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY)
    {
        if (this.tooltip != null)
        {
            if (active && visible && hovered)
            {
                if (mouseX == tooltipX && mouseY == tooltipY) tooltipTimer += MinecraftClient.getInstance().getTickDelta();
                else tooltipTimer = 0;
                
                tooltipX = mouseX;
                tooltipY = mouseY;
                if (tooltipTimer >= tooltipDelay) KeystoneOverlayHandler.addTooltip(this.tooltip);
            }
            else tooltipTimer = 0;
        }
    }
}
