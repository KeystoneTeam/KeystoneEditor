package keystone.core.gui.widgets.groups;

import keystone.core.DebugFlags;
import keystone.core.gui.GUIMaskStack;
import keystone.core.gui.widgets.LocationListener;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LayoutGroup extends WidgetList
{
    public record PinnedLocation(ClickableWidget relativeTo, int offsetX, int offsetY)
    {
        public PinnedLocation(ClickableWidget pinnedWidget, ClickableWidget relativeTo)
        {
            this(relativeTo, pinnedWidget.x - relativeTo.x, pinnedWidget.y - relativeTo.y);
        }
    }
    
    private Margins margins;
    private boolean maskGroup;
    private boolean baked;
    
    protected final List<ClickableWidget> layoutControlled;
    protected final Map<ClickableWidget, PinnedLocation> pinMap;
    
    public LayoutGroup(int x, int y, int width, int height, Text message)
    {
        this(x, y, width, height, message, false);
    }
    public LayoutGroup(int x, int y, int width, int height, Text message, boolean maskGroup)
    {
        super(x, y, width, height, message);
        this.margins = new Margins(0);
        this.maskGroup = maskGroup;
        this.layoutControlled = new ArrayList<>();
        this.pinMap = new HashMap<>();
    }
    
    public void bake()
    {
        if (!baked)
        {
            this.baked = true;
            updateLayout();
            postBake(layoutControlled);
        }
    }
    
    /**
     * Apply this layout to a list of widgets. This function should be applied assuming
     * (0, 0) is the top-left corner of the display area
     * @param widgets The list of widgets to apply the layout to
     */
    protected abstract void applyLayout(List<ClickableWidget> widgets);
    
    /**
     * Ran after this layout group is baked. Use this for one-time initialization, such
     * as the height of the group or a maximum scroll amount
     * @param widgets The list of widgets that this group was baked with and are layout controlled
     */
    protected void postBake(List<ClickableWidget> widgets) { }
    
    protected void prepareRender(MatrixStack matrices, int mouseX, int mouseY, float delta) { }
    protected void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) { }
    protected void renderForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) { }
    
    protected void renderDebug(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        // Individual Widget Bounds
        for (ClickableWidget widget : layoutControlled) fill(matrices, widget.x, widget.y, widget.x + widget.getWidth(), widget.y + widget.getHeight(), 0x80FF0000);
    
        // Display Bounds
        fill(matrices, x + 2, y, x + width - 2, y + 2, 0x800000FF);
        fill(matrices, x + width - 2, y, x + width, y + height, 0x800000FF);
        fill(matrices, x + 2, y + height - 2, x + width - 2, y + height, 0x800000FF);
        fill(matrices, x, y, x + 2, y + height, 0x800000FF);
    }
    
    //region Layout Application
    public void updateLayout()
    {
        // Apply the layouts of any child layout
        for (ClickableWidget widget : children)
        {
            if (widget instanceof LayoutGroup layoutGroup)
            {
                layoutGroup.applyLayout(layoutGroup.children);
            }
        }
        
        // Apply the layout
        applyLayout(layoutControlled);
    
        // Move widgets to the correct offset
        correctOffset();
    }
    private void correctOffset()
    {
        // Move widgets to the correct offset
        for (ClickableWidget widget : layoutControlled)
        {
            widget.x += this.x + margins.left();
            widget.y += this.y + margins.top();
        
            // Correct the element offsets of any child layout groups
            if (widget instanceof LayoutGroup layoutGroup) layoutGroup.correctOffset();
            if (widget instanceof LocationListener locationListener) locationListener.onLocationChanged(widget.x, widget.y, widget.getWidth(), widget.getHeight());
        }
        
        // Update pin locations
        for (Map.Entry<ClickableWidget, PinnedLocation> pin : pinMap.entrySet())
        {
            ClickableWidget pinned = pin.getKey();
            PinnedLocation pinnedLocation = pin.getValue();
            pinned.x = pinnedLocation.relativeTo.x + pinnedLocation.offsetX;
            pinned.y = pinnedLocation.relativeTo.y + pinnedLocation.offsetY;
    
            // Correct the element offsets of any child layout groups
            if (pinned instanceof LayoutGroup layoutGroup) layoutGroup.correctOffset();
            if (pinned instanceof LocationListener locationListener) locationListener.onLocationChanged(pinned.x, pinned.y, pinned.getWidth(), pinned.getHeight());
        }
    }
    
    /**
     * Apply a new position and width to the provided widget
     * @param widget The widget to change
     * @param x The new x-coordinate
     * @param y The new y-coordinate
     * @param width The new width
     * @return Any difference in the bottom y-coordinate of the widget. This is used to correct poorly written vanilla widget code
     */
    protected int applyLayout(ClickableWidget widget, int x, int y, int width)
    {
        // Apply new position and width
        widget.x = x;
        widget.y = y;
        widget.setWidth(width);
        
        return 0;
    }
    //endregion
    //region Pinning
    public void addLayoutIgnoredWidget(ClickableWidget widget)
    {
        addPinnedWidget(widget, this);
    }
    public void addPinnedWidget(ClickableWidget pinned, ClickableWidget relativeTo)
    {
        addPinnedWidget(pinned, relativeTo != null ? new PinnedLocation(pinned, relativeTo) : new PinnedLocation(pinned, this));
    }
    public void addPinnedWidget(ClickableWidget pinned, PinnedLocation pinnedLocation)
    {
        this.pinMap.put(pinned, pinnedLocation);
        add(pinned);
    }
    //endregion
    //region Helpers
    public void move(int newX, int newY)
    {
        this.x = newX;
        this.y = newY;
        updateLayout();
    }
    //endregion
    //region WidgetList Overrides
    @Override
    public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        if (visible)
        {
            // Prepare Render
            matrices.push();
            prepareRender(matrices, mouseX, mouseY, delta);
    
            // Render Background
            GUIMaskStack.push(true);
            renderBackground(matrices, mouseX, mouseY, delta);
            GUIMaskStack.pop();
            
            // Start Masking
            if (maskGroup)
            {
                GUIMaskStack.push(true);
                GUIMaskStack.addMaskBox(matrices, x + margins.left(), y + margins.top(), width - margins.left() - margins.right(), height);
            }
            
            // Render Widgets
            for (ClickableWidget widget : layoutControlled) widget.render(matrices, mouseX, mouseY, delta);
            for (ClickableWidget widget : pinMap.keySet()) if (widget.visible) widget.render(matrices, mouseX, mouseY, delta);
            
            // End Masking
            if (maskGroup) GUIMaskStack.pop();
            
            // Render Foreground and Debug
            renderForeground(matrices, mouseX, mouseY, delta);
            if (DebugFlags.isFlagSet("debugLayoutGroups")) renderDebug(matrices, mouseX, mouseY, delta);
            
            // Finish Render
            matrices.pop();
        }
    }
    @Override
    public void add(ClickableWidget element)
    {
        super.add(element);
        if (!pinMap.containsKey(element)) layoutControlled.add(element);
        if (baked && !pinMap.containsKey(element))
        {
            updateLayout();
            postBake(layoutControlled);
        }
    }
    @Override
    public void addToTop(ClickableWidget element)
    {
        super.addToTop(element);
        if (!pinMap.containsKey(element)) layoutControlled.add(0, element);
        if (baked && !pinMap.containsKey(element))
        {
            updateLayout();
            postBake(layoutControlled);
        }
    }
    @Override
    public void remove(ClickableWidget element)
    {
        super.remove(element);
        if (pinMap.containsKey(element)) pinMap.remove(element);
        else
        {
            layoutControlled.remove(element);
            if (baked)
            {
                updateLayout();
                postBake(layoutControlled);
            }
        }
    }
    //endregion
    //region Getters and Setters
    protected boolean isBaked() { return baked; }
    
    public Margins getMargins() { return margins; }
    public int layoutControlledWidgetCount() { return layoutControlled.size(); }
    public int layoutIgnoredWidgetCount() { return children.size() - layoutControlled.size(); }
    
    public void setMargins(Margins margins)
    {
        this.margins = margins;
        if (baked)
        {
            updateLayout();
            postBake(layoutControlled);
        }
    }
    public void setMaskGroup(boolean maskGroup) { this.maskGroup = maskGroup; }
    //endregion
}
