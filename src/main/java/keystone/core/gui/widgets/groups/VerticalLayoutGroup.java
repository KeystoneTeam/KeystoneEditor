package keystone.core.gui.widgets.groups;

import keystone.core.KeystoneConfig;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class VerticalLayoutGroup extends LayoutGroup
{
    public enum Alignment { LEFT, CENTER, RIGHT, NONE }
    
    private int padding;
    private int maxHeight;
    private Alignment alignment;
    private int scrollOffset;
    private int maxScrollOffset;
    
    public VerticalLayoutGroup(int x, int y, int width, int maxHeight, int padding, Text message)
    {
        this(x, y, width, maxHeight, padding, message, Alignment.LEFT);
    }
    public VerticalLayoutGroup(int x, int y, int width, int maxHeight, int padding, Text message, Alignment alignment)
    {
        super(x, y, width, 0, message, true);
        this.padding = padding;
        this.maxHeight = maxHeight;
        this.alignment = alignment;
    }
    
    @Override
    protected void applyLayout(List<ClickableWidget> widgets)
    {
        // Apply Layout
        int y = -scrollOffset;
        for (ClickableWidget widget : widgets)
        {
            int x = switch(alignment)
            {
                case CENTER -> (width - widget.getWidth()) / 2;
                case RIGHT -> width - widget.getWidth();
                default -> 0;
            };
            applyLayout(widget, x, y, widget.getWidth());
            y += widget.getHeight() + padding;
        }
        this.height = Math.min(y - padding, maxHeight);
    
        // Update Max Scroll Index
        boolean hadScrollbar = maxScrollOffset > 0;
        maxScrollOffset = 0;
        for (ClickableWidget widget : layoutControlled) maxScrollOffset += widget.getHeight() + padding;
        maxScrollOffset = Math.max(0, maxScrollOffset - padding - height);
    
        // Update width based on scrollbar
        if (maxScrollOffset > 0 && !hadScrollbar) width += 2;
        else if (maxScrollOffset == 0 && hadScrollbar) width -= 2;
    }
    
    @Override
    protected void renderForeground(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        if (maxScrollOffset > 0)
        {
            float heightProportion = height / (float)(height + maxScrollOffset);
            int scrollbarHeight = (int)(heightProportion * height);
            int scrollbarY = y + (int)(heightProportion * scrollOffset);
            fill(matrices, x + width - 2, scrollbarY, x + width, scrollbarY + scrollbarHeight, 0xFF808080);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
    {
        if (!active || !visible) return false;
        if (super.mouseScrolled(mouseX, mouseY, scrollDelta)) return true;
        return scrollPanel(mouseX, mouseY, scrollDelta);
    }
    
    private boolean scrollPanel(double mouseX, double mouseY, double scrollDelta)
    {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
        {
            scrollOffset -= Math.signum(scrollDelta) * KeystoneConfig.guiScrollSpeed;
            scrollOffset = Math.min(maxScrollOffset, Math.max(0, scrollOffset));
            updateLayout();
            return true;
        }
        return false;
    }
    
    public int getPadding() { return padding; }
    public int getMaxHeight() { return maxHeight; }
    public Alignment getAlignment() { return alignment; }
    public void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
        if (isBaked()) updateLayout();
    }
    public void setAlignment(Alignment alignment)
    {
        this.alignment = alignment;
        if (isBaked()) updateLayout();
    }
    public void setPadding(int padding)
    {
        this.padding = padding;
        if (isBaked()) updateLayout();
    }
}
