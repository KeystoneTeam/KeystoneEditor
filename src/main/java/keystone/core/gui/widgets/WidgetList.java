package keystone.core.gui.widgets;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetList extends ClickableWidget
{
    private int scrollIndex;
    private int maxScrollIndex;
    private int offsetX;
    private int offsetY;

    protected int maxHeight;

    private final int padding;
    private final List<ClickableWidget> currentWidgets = new ArrayList<>();
    private final List<ClickableWidget> widgets = new ArrayList<>();
    private final List<ClickableWidget> queuedWidgets = new ArrayList<>();

    public WidgetList(int x, int y, int width, int maxHeight, int padding, Text label)
    {
        super(x, y, width, 0, label);
        this.padding = padding;
        this.maxHeight = maxHeight;
    }

    public void add(ClickableWidget widget) { add(widget, false); }
    public void add(ClickableWidget widget, boolean queued)
    {
        widget.x += this.x + padding;

        if (queued) queuedWidgets.add(widget);
        else widgets.add(widget);
    }
    public void clear()
    {
        this.currentWidgets.clear();
        this.widgets.clear();
        this.queuedWidgets.clear();
    }

    public void bake()
    {
        // Check for empty list
        if (widgets.size() == 0 && queuedWidgets.size() == 0)
        {
            height = 0;
            return;
        }
        
        // Add listeners to all changing height widgets
        for (ClickableWidget widget : widgets)
        {
            if (widget instanceof IChangingHeightWidget changingHeightWidget)
            {
                changingHeightWidget.addListener((w, height) -> updateCurrentWidgets());
            }
        }

        scrollIndex = 0;
        updateCurrentWidgets();
    }
    public void move(int x, int y)
    {
        for (ClickableWidget widget : queuedWidgets)
        {
            widget.x += x;
            widget.y += y;
        }
        this.x += x;
        this.y += y;
        updateCurrentWidgets();
    }
    public void setElementsOffset(int x, int y)
    {
        offsetX = x;
        offsetY = y;
        updateCurrentWidgets();
    }
    public void forEach(Consumer<ClickableWidget> consumer)
    {
        currentWidgets.forEach(clickable ->
        {
            if (clickable instanceof WidgetList list) list.forEach(consumer);
            consumer.accept(clickable);
        });
    }

    private boolean scrollPanel(double mouseX, double mouseY, double scrollDelta)
    {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
        {
            scrollIndex += scrollDelta < 0 ? 1 : -1;
            if (scrollIndex < 0) scrollIndex = 0;
            if (scrollIndex > maxScrollIndex) scrollIndex = maxScrollIndex;

            updateCurrentWidgets();
            return true;
        }
        return false;
    }

    protected void updateCurrentWidgets()
    {
        // Update Child Lists
        for (ClickableWidget widget : queuedWidgets) if (widget instanceof WidgetList list) list.updateCurrentWidgets();
        for (ClickableWidget widget : currentWidgets) if (widget instanceof WidgetList list) list.updateCurrentWidgets();

        // Update Max Scroll Index
        int restoreScrollIndex = scrollIndex;
        boolean hadScrollbar = maxScrollIndex > 0;
        scrollIndex = 0;
        maxScrollIndex = 0;
        while (true)
        {
            updateWidgetLocations();
            if (currentWidgets.size() + scrollIndex >= widgets.size())
            {
                maxScrollIndex = scrollIndex;
                break;
            }
            else scrollIndex++;
        }
        scrollIndex = restoreScrollIndex;
        if (maxScrollIndex > 0 && !hadScrollbar) width += 2;
        else if (maxScrollIndex == 0 && hadScrollbar) width -= 2;

        updateWidgetLocations();
    }
    private void updateWidgetLocations()
    {
        currentWidgets.clear();
        height = 0;
        
        int x = this.x + offsetX;
        int y = this.y + offsetY;
        int i = scrollIndex;
        
        // Update Widget Locations
        while (i < widgets.size())
        {
            ClickableWidget widget = widgets.get(i);
            if (height + widget.getHeight() > maxHeight) break;
            widget.x = x;
            widget.y = y;
            
            y += widget.getHeight() + padding;
            height += widget.getHeight() + padding;
            currentWidgets.add(widget);
            i++;
        }
    }
    
    //region Widget Overrides
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (!visible) return;

        if (currentWidgets.size() != widgets.size())
        {
            int scrollbarY = y + (int)(height * (scrollIndex / (float)widgets.size()));
            int scrollbarHeight = (int)(height * (currentWidgets.size() / (float)widgets.size()));
            fill(stack, x + width - 1, scrollbarY, x + width + 1, scrollbarY + scrollbarHeight, 0xFF808080);
        }

        stack.push();
        currentWidgets.forEach(widget -> { if (widget.visible) widget.render(stack, mouseX, mouseY, partialTicks); });
        queuedWidgets.forEach(widget -> { if (widget.visible) widget.render(stack, mouseX, mouseY, partialTicks); });
        stack.pop();
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        if (!active) return;
        currentWidgets.forEach(widget -> { if (widget.active) widget.onClick(mouseX, mouseY); });
        queuedWidgets.forEach(widget -> { if (widget.active) widget.onClick(mouseX, mouseY); });
    }

    @Override
    public void onRelease(double mouseX, double mouseY)
    {
        if (!active) return;
        currentWidgets.forEach(widget -> { if (widget.active) widget.onRelease(mouseX, mouseY); });
        queuedWidgets.forEach(widget -> { if (widget.active) widget.onRelease(mouseX, mouseY); });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseClicked(mouseX, mouseY, button)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseReleased(mouseX, mouseY, button)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.mouseReleased(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return false;
    }

    @Override
    public boolean isHovered()
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.isHovered()) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.isHovered()) return true;
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.isMouseOver(mouseX, mouseY)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.isMouseOver(mouseX, mouseY)) return true;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY)
    {
        if (!active) return;
        currentWidgets.forEach(widget -> { if (widget.active) widget.mouseMoved(mouseX, mouseY); });
        queuedWidgets.forEach(widget -> { if (widget.active) widget.mouseMoved(mouseX, mouseY); });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseScrolled(mouseX, mouseY, scrollDelta)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.mouseScrolled(mouseX, mouseY, scrollDelta)) return true;
        return scrollPanel(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.charTyped(codePoint, modifiers)) return true;
        for (ClickableWidget widget : currentWidgets) if (widget.active && widget.charTyped(codePoint, modifiers)) return true;
        return false;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {

    }
    //endregion
}
