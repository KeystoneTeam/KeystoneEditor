package keystone.core.gui.widgets.groups;

import keystone.core.KeystoneConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
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
    protected void postBake(List<ClickableWidget> widgets)
    {
        if (widgets.size() == 0)
        {
            this.height = 0;
            this.maxScrollOffset = 0;
        }
        else
        {
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            
            for (ClickableWidget widget : widgets)
            {
                minY = Math.min(minY, widget.getY());
                maxY = Math.max(maxY, widget.getY() + widget.getHeight());
            }
            
            this.height = Math.min(maxY - minY, maxHeight);
            this.maxScrollOffset = Math.max(0, maxY - minY - height);
        }
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
            y += widget.getHeight() + padding + applyLayout(widget, x, y, widget.getWidth());
        }
    }
    
    @Override
    protected void renderForeground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        if (maxScrollOffset > 0)
        {
            float heightProportion = height / (float)(height + maxScrollOffset);
            int scrollbarHeight = (int)(heightProportion * height);
            int scrollbarY = getY() + (int)(heightProportion * scrollOffset);
            context.fill(getX() + width - 2, scrollbarY, getX() + width, scrollbarY + scrollbarHeight, 0xFF808080);
        }
    }
    @Override
    protected void renderDebug(DrawContext context, int mouseX, int mouseY, float delta)
    {
        int minY = this.getY() - scrollOffset;
        int maxY = minY + this.height + this.maxScrollOffset;
    
        // Individual Widget Bounds
        for (ClickableWidget widget : layoutControlled) context.fill(widget.getX(), widget.getY(), widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight(), 0x80FF0000);
    
        // Content Bounds
        context.fill(getX() + 2, minY, getX() + width - 2, minY + 2, 0x8000FF00);
        context.fill(getX() + width - 2, minY, getX() + width, maxY, 0x8000FF00);
        context.fill(getX() + 2, maxY - 2, getX() + width - 2, maxY, 0x8000FF00);
        context.fill(getX(), minY, getX() + 2, maxY, 0x8000FF00);
    
        // Display Bounds
        context.fill(getX() + 2, getY(), getX() + width - 2, getY() + 2, 0x800000FF);
        context.fill(getX() + width - 2, getY(), getX() + width, getY() + height, 0x800000FF);
        context.fill(getX() + 2, getY() + height - 2, getX() + width - 2, getY() + height, 0x800000FF);
        context.fill(getX(), getY(), getX() + 2, getY() + height, 0x800000FF);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (!active || !visible) return false;
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        return scrollPanel(mouseX, mouseY, verticalAmount);
    }
    private boolean scrollPanel(double mouseX, double mouseY, double scrollDelta)
    {
        if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() && mouseY <= getY() + height)
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
