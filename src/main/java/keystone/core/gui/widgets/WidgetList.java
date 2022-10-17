package keystone.core.gui.widgets;

import keystone.core.KeystoneConfig;
import keystone.core.gui.GUIMaskHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetList extends ClickableWidget
{
    private int scrollOffset;
    private int maxScrollOffset;
    private int offsetX;
    private int offsetY;

    protected int maxHeight;

    private final int padding;
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

        scrollOffset = 0;
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
        widgets.forEach(clickable ->
        {
            if (clickable instanceof WidgetList list) list.forEach(consumer);
            consumer.accept(clickable);
        });
    }

    private boolean scrollPanel(double mouseX, double mouseY, double scrollDelta)
    {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
        {
            scrollOffset -= Math.signum(scrollDelta) * KeystoneConfig.guiScrollSpeed;
            scrollOffset = Math.min(maxScrollOffset, Math.max(0, scrollOffset));
            updateCurrentWidgets();
            return true;
        }
        return false;
    }

    protected void updateCurrentWidgets()
    {
        // Update Child Lists
        for (ClickableWidget widget : queuedWidgets) if (widget instanceof WidgetList list) list.updateCurrentWidgets();
        for (ClickableWidget widget : widgets) if (widget instanceof WidgetList list) list.updateCurrentWidgets();

        // Update Max Scroll Index
        boolean hadScrollbar = maxScrollOffset > 0;
        maxScrollOffset = 0;
        
        for (ClickableWidget widget : widgets) maxScrollOffset += widget.getHeight() + padding;
        maxScrollOffset = Math.max(0, maxScrollOffset - padding - height);
        
        if (maxScrollOffset > 0 && !hadScrollbar) width += 2;
        else if (maxScrollOffset == 0 && hadScrollbar) width -= 2;

        // Update Widget Locations
        updateWidgetLocations();
    }
    private void updateWidgetLocations()
    {
        height = 0;
        int x = this.x + offsetX;
        int y = this.y + offsetY - scrollOffset;
        
        // Update Widget Locations
        for (ClickableWidget widget : widgets)
        {
            widget.x = x;
            widget.y = y;
    
            y += widget.getHeight() + padding;
            height += widget.getHeight() + padding;
        }
        height = Math.min(height, maxHeight);
    }
    
    //region Widget Overrides
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (!visible) return;

        //if (maxScrollOffset > 0)
        //{
        //    int scrollbarY = y + (int)(height * (scrollIndex / (float)widgets.size()));
        //    int scrollbarHeight = (int)(height * (currentWidgets.size() / (float)widgets.size()));
        //    fill(stack, x + width - 1, scrollbarY, x + width + 1, scrollbarY + scrollbarHeight, 0xFF808080);
        //}
        if (maxScrollOffset > 0)
        {
            int scrollbarHeight = height - maxScrollOffset;
            int scrollbarY = y + scrollOffset;
            fill(stack, x + width - 2, scrollbarY, x + width, scrollbarY + scrollbarHeight, 0xFF808080);
        }

        stack.push();
        GUIMaskHelper.addMask(stack, x, y, width, height);
        widgets.forEach(widget -> { if (widget.visible) widget.render(stack, mouseX, mouseY, partialTicks); });
        queuedWidgets.forEach(widget -> { if (widget.visible) widget.render(stack, mouseX, mouseY, partialTicks); });
        GUIMaskHelper.endMask();
        stack.pop();
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        if (!clicked(mouseX, mouseY)) return;
        widgets.forEach(widget -> { if (widget.active) widget.onClick(mouseX, mouseY); });
        queuedWidgets.forEach(widget -> { if (widget.active) widget.onClick(mouseX, mouseY); });
    }

    @Override
    public void onRelease(double mouseX, double mouseY)
    {
        if (!clicked(mouseX, mouseY)) return;
        widgets.forEach(widget -> { if (widget.active) widget.onRelease(mouseX, mouseY); });
        queuedWidgets.forEach(widget -> { if (widget.active) widget.onRelease(mouseX, mouseY); });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!clicked(mouseX, mouseY)) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseClicked(mouseX, mouseY, button)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (!clicked(mouseX, mouseY)) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseReleased(mouseX, mouseY, button)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.mouseReleased(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (!clicked(mouseX, mouseY)) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return false;
    }

    @Override
    public boolean isHovered()
    {
        Mouse mouse = MinecraftClient.getInstance().mouse;
        Window window = MinecraftClient.getInstance().getWindow();
        if (!clicked(mouse.getX() * window.getScaledWidth() / window.getWidth(), mouse.getY() * window.getScaledHeight() / window.getHeight())) return false;
        
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.isHovered()) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.isHovered()) return true;
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        if (!clicked(mouseX, mouseY)) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.isMouseOver(mouseX, mouseY)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.isMouseOver(mouseX, mouseY)) return true;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY)
    {
        if (!clicked(mouseX, mouseY)) return;
        widgets.forEach(widget -> { if (widget.active) widget.mouseMoved(mouseX, mouseY); });
        queuedWidgets.forEach(widget -> { if (widget.active) widget.mouseMoved(mouseX, mouseY); });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
    {
        if (!clicked(mouseX, mouseY)) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.mouseScrolled(mouseX, mouseY, scrollDelta)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.mouseScrolled(mouseX, mouseY, scrollDelta)) return true;
        return scrollPanel(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!active) return false;
        for (ClickableWidget widget : queuedWidgets) if (widget.active && widget.charTyped(codePoint, modifiers)) return true;
        for (ClickableWidget widget : widgets) if (widget.active && widget.charTyped(codePoint, modifiers)) return true;
        return false;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {

    }
    //endregion
}
