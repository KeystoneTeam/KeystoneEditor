package keystone.core.gui.widgets.groups;

import keystone.core.gui.widgets.ITickableWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetList extends ClickableWidget implements ParentElement, ITickableWidget
{
    private ClickableWidget focused;
    private boolean dragging;
    protected final List<ClickableWidget> children;
    
    public WidgetList(int x, int y, int width, int height, Text message)
    {
        super(x, y, width, height, message);
        this.children = new ArrayList<>();
    }
    
    //region Parent Element Implementation
    @Override public List<? extends Element> children() { return this.children; }
    @Override public boolean isDragging() { return this.dragging; }
    @Override public void setDragging(boolean dragging) { this.dragging = dragging; }
    @Nullable @Override public Element getFocused() { return this.focused; }
    @Override
    public void setFocused(@Nullable Element focused)
    {
        assert focused == null || focused instanceof ClickableWidget;
        if (this.focused != null) this.focused.setFocused(false);
        if (focused != null) focused.setFocused(true);
        this.focused = (ClickableWidget) focused;
    }
    
    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder)
    {
        Screen.SelectedElementNarrationData selectedElementNarrationData = Screen.findSelectedElementData(children, this.focused);
        if (selectedElementNarrationData != null)
        {
            if (children.size() > 1)
            {
                builder.put(NarrationPart.POSITION, Text.translatable("narrator.position.object_list", selectedElementNarrationData.index + 1, children.size()));
                if (selectedElementNarrationData.selectType == SelectionType.FOCUSED) builder.put(NarrationPart.USAGE, Text.translatable("narration.component_list.usage"));
            }
            selectedElementNarrationData.selectable.appendNarrations(builder.nextMessage());
        }
    }
    //endregion
    //region ITickableWidget Implementation
    @Override
    public void tick()
    {
        for (Element widget : children)
        {
            if (widget instanceof ITickableWidget tickable) tickable.tick();
        }
    }
    //endregion
    //region Widget Overrides
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        if (visible)
        {
            for (ClickableWidget child : children) child.render(context, mouseX, mouseY, delta);
        }
    }
    
    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) { }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        if (!active || !visible) return false;
        for (ClickableWidget widget : children) if (widget.active && widget.isMouseOver(mouseX, mouseY)) return true;
        return super.isMouseOver(mouseX, mouseY);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (active && visible) return ParentElement.super.mouseClicked(mouseX, mouseY, button);
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (active && visible) return ParentElement.super.mouseReleased(mouseX, mouseY, button);
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (active && visible) return ParentElement.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (active && visible) return ParentElement.super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (active && visible) return ParentElement.super.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (active && visible) return ParentElement.super.keyReleased(keyCode, scanCode, modifiers);
        return false;
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (active && visible) return ParentElement.super.charTyped(chr, modifiers);
        return false;
    }
    //endregion
    
    public List<ClickableWidget> clickableChildren()
    {
        return children;
    }
    public void forEach(Consumer<ClickableWidget> consumer)
    {
        children.forEach(consumer);
    }
    public void add(ClickableWidget element)
    {
        children.add(element);
    }
    public void addToTop(ClickableWidget element)
    {
        children.add(0, element);
    }
    public void remove(ClickableWidget element)
    {
        children.remove(element);
    }
    public void clear()
    {
        children.clear();
    }
    public int childCount()
    {
        return children.size();
    }
}
