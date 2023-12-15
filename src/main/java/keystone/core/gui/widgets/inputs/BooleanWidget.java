package keystone.core.gui.widgets.inputs;

import keystone.core.KeystoneConfig;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

public class BooleanWidget extends CheckboxWidget
{
    private IKeystoneTooltip tooltip;
    private float tooltipDelay;
    private float tooltipTimer;
    private int tooltipX;
    private int tooltipY;

    public BooleanWidget(int x, int y, int width, int height, Text message, boolean checked)
    {
        super(x, y, message, MinecraftClient.getInstance().textRenderer, checked, (checkbox, newChecked) -> ((BooleanWidget) checkbox).onChanged(newChecked));
        this.width = width;
        this.height = height;
        this.tooltipDelay = KeystoneConfig.tooltipDelay;
    }
    public BooleanWidget setTooltip(IKeystoneTooltip tooltip) { this.tooltip = tooltip; return this; }
    public BooleanWidget setTooltipDelay(float delay) { this.tooltipDelay = delay; return this; }
    
    public void onChanged(boolean checked) {}
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderWidget(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
    public void renderTooltip(DrawContext context, int mouseX, int mouseY)
    {
        if (this.tooltip != null)
        {
            if (active && visible && hovered)
            {
                if (mouseX == tooltipX && mouseY == tooltipY) tooltipTimer += MinecraftClient.getInstance().getTickDelta();
                else if (tooltipTimer < tooltipDelay) tooltipTimer = 0;
                
                tooltipX = mouseX;
                tooltipY = mouseY;
                if (tooltipTimer >= tooltipDelay) KeystoneOverlayHandler.addTooltip(this.tooltip);
            }
            else tooltipTimer = 0;
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!isFocused()) return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
