package keystone.core.gui.widgets;

import keystone.core.KeystoneConfig;
import keystone.core.gui.GUIMaskHelper;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetList extends ClickableWidget implements ParentElement
{
    private int scrollOffset;
    private int maxScrollOffset;
    private int offsetX;
    private int offsetY;

    protected int maxHeight;

    private final int padding;
    private final List<ClickableWidget> layoutControlledWidgets = new ArrayList<>();
    private final List<ClickableWidget> nonLayoutControlledWidgets = new ArrayList<>();
    private final List<ClickableWidget> allWidgets = new ArrayList<>();
    private ClickableWidget focused;
    private boolean dragging;

    public WidgetList(int x, int y, int width, int maxHeight, int padding, Text label)
    {
        super(x, y, width, 0, label);
        this.padding = padding;
        this.maxHeight = maxHeight;
    }
    
    //region Parent Element Implementation
    public final boolean isDragging() {
        return this.dragging;
    }
    
    public final void setDragging(boolean dragging) {
        this.dragging = dragging;
    }
    
    @Nullable
    public Element getFocused() {
        return this.focused;
    }
    
    public void setFocused(@Nullable Element focused) {
        this.focused = (ClickableWidget)focused;
    }
    
    @Override
    public List<? extends Element> children()
    {
        return layoutControlledWidgets;
    }
    
    @Override
    public boolean changeFocus(boolean lookForwards)
    {
        boolean successful = ParentElement.super.changeFocus(lookForwards);
        while (successful && focused instanceof FieldWidgetList.HeaderWidget) successful = changeFocus(lookForwards);
        return successful;
    }
    //endregion

    public void add(ClickableWidget widget) { add(widget, true); }
    public void add(ClickableWidget widget, boolean layoutControlled)
    {
        widget.x += this.x + offsetX;
        if (widget instanceof TextFieldWidget textField) textField.setWidth(textField.getWidth() - 2);

        if (layoutControlled) layoutControlledWidgets.add(widget);
        else nonLayoutControlledWidgets.add(widget);
        allWidgets.add(widget);
    }
    public void clear()
    {
        this.layoutControlledWidgets.clear();
        this.nonLayoutControlledWidgets.clear();
        this.allWidgets.clear();
    }
    public int layoutControlledWidgetCount() { return layoutControlledWidgets.size(); }
    public int nonLayoutControlledWidgetCount() { return nonLayoutControlledWidgets.size(); }
    public int allWidgetCount() { return allWidgets.size(); }

    public void bake()
    {
        // Check for empty list
        if (allWidgets.size() == 0)
        {
            height = 0;
            return;
        }
        
        // Add listeners to all changing height widgets
        for (ClickableWidget widget : layoutControlledWidgets)
        {
            if (widget instanceof ILocationObservable locationObservable)
            {
                locationObservable.addListener((x, y, width, height) -> updateCurrentWidgets());
            }
        }

        scrollOffset = 0;
        updateCurrentWidgets();
    }
    public void move(int x, int y)
    {
        for (ClickableWidget widget : nonLayoutControlledWidgets)
        {
            widget.x += x;
            widget.y += y;
        }
        this.x += x;
        this.y += y;
        updateCurrentWidgets();
    }
    public boolean isMouseInListArea(double mouseX, double mouseY)
    {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public void setElementsOffset(int x, int y)
    {
        offsetX = x;
        offsetY = y;
        updateCurrentWidgets();
    }
    public void forEachLayoutControlled(Consumer<ClickableWidget> consumer)
    {
        layoutControlledWidgets.forEach(clickable ->
        {
            if (clickable instanceof WidgetList list) list.forEachLayoutControlled(consumer);
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
        for (ClickableWidget widget : allWidgets) if (widget instanceof WidgetList list) list.updateCurrentWidgets();
    
        // Update Widget Locations
        updateWidgetLocations();
        
        // Update Max Scroll Index
        boolean hadScrollbar = maxScrollOffset > 0;
        maxScrollOffset = 0;
        
        for (ClickableWidget widget : layoutControlledWidgets) maxScrollOffset += widget.getHeight() + padding;
        maxScrollOffset = Math.max(0, maxScrollOffset - padding - height);
        
        if (maxScrollOffset > 0 && !hadScrollbar) width += 2;
        else if (maxScrollOffset == 0 && hadScrollbar) width -= 2;
    }
    private void updateWidgetLocations()
    {
        height = 0;
        int x = this.x + offsetX;
        int y = this.y + offsetY - scrollOffset;
        
        // Update Widget Locations
        for (ClickableWidget widget : layoutControlledWidgets)
        {
            widget.x = x;
            widget.y = y;
            if (widget instanceof TextFieldWidget textField) textField.x++;
            if (widget instanceof ILocationObservable locationObservable) locationObservable.trigger(widget);

            y += widget.getHeight() + padding;
            height += widget.getHeight() + padding;
        }
        height = Math.min(height, maxHeight);

        if (this instanceof ILocationObservable locationObservable) locationObservable.trigger(this);
    }
    
    //region Widget Overrides
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (!visible) return;
        
        if (maxScrollOffset > 0)
        {
            float heightProportion = height / (float)(height + maxScrollOffset);
            int scrollbarHeight = (int)(heightProportion * height);
            int scrollbarY = y + (int)(heightProportion * scrollOffset);
            fill(stack, x + width - 2, scrollbarY, x + width, scrollbarY + scrollbarHeight, 0xFF808080);
        }

        stack.push();
        GUIMaskHelper.addMask(stack, x, y, width, height);
        layoutControlledWidgets.forEach(widget -> { if (widget.visible) widget.render(stack, mouseX, mouseY, partialTicks); });
        nonLayoutControlledWidgets.forEach(widget -> { if (widget.visible) widget.render(stack, mouseX, mouseY, partialTicks); });
        GUIMaskHelper.endMask();
        stack.pop();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!active || !visible) return false;
        if (focused != null && focused.active && focused.mouseClicked(mouseX, mouseY, button)) return true;
        
        boolean mouseInListArea = isMouseInListArea(mouseX, mouseY);
        for (ClickableWidget widget : allWidgets)
        {
            if (!mouseInListArea && layoutControlledWidgets.contains(widget)) continue;
            
            if (widget != focused && widget.active && widget.mouseClicked(mouseX, mouseY, button))
            {
                setFocused(widget);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (!active || !visible) return false;
        if (focused != null && focused.active && focused.mouseReleased(mouseX, mouseY, button)) return true;
    
        boolean mouseInListArea = isMouseInListArea(mouseX, mouseY);
        for (ClickableWidget widget : allWidgets)
        {
            if (!mouseInListArea && layoutControlledWidgets.contains(widget)) continue;
            if (widget != focused && widget.active && widget.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (!active || !visible) return false;
        if (focused != null && focused.active && focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
    
        boolean mouseInListArea = isMouseInListArea(mouseX, mouseY);
        for (ClickableWidget widget : allWidgets)
        {
            if (!mouseInListArea && layoutControlledWidgets.contains(widget)) continue;
            if (widget != focused && widget.active && widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
    public boolean isHovered()
    {
        if (!active || !visible) return false;
        for (ClickableWidget widget : allWidgets) if (widget.active && widget.isHovered()) return true;
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        if (!active || !visible) return false;
        for (ClickableWidget widget : allWidgets) if (widget.active && widget.isMouseOver(mouseX, mouseY)) return true;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY)
    {
        if (!active || !visible) return;
        allWidgets.forEach(widget -> { if (widget.active) widget.mouseMoved(mouseX, mouseY); });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
    {
        if (!active || !visible) return false;
        if (focused != null && focused.active && focused.mouseScrolled(mouseX, mouseY, scrollDelta)) return true;
    
        boolean mouseInListArea = isMouseInListArea(mouseX, mouseY);
        for (ClickableWidget widget : allWidgets)
        {
            if (!mouseInListArea && layoutControlledWidgets.contains(widget)) continue;
            if (widget != focused && widget.active && widget.mouseScrolled(mouseX, mouseY, scrollDelta)) return true;
        }
        return scrollPanel(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!active || !visible) return false;
        if (focused != null && focused.active && focused.keyPressed(keyCode, scanCode, modifiers)) return true;
        for (ClickableWidget widget : allWidgets) if (widget != focused && widget.active && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (!active || !visible) return false;
        if (focused != null && focused.active && focused.keyReleased(keyCode, scanCode, modifiers)) return true;
        for (ClickableWidget widget : allWidgets) if (widget != focused && widget.active && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (!active || !visible) return false;
        if (focused != null && focused.active && focused.charTyped(codePoint, modifiers)) return true;
        for (ClickableWidget widget : allWidgets) if (widget != focused && widget.active && widget.charTyped(codePoint, modifiers)) return true;
        return false;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {
    
    }
    //endregion
}
