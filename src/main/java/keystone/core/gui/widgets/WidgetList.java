package keystone.core.gui.widgets;

import net.minecraft.client.gui.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetList
{
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    private List<Widget> widgets = new ArrayList<>();
    private List<Widget> queuedWidgets = new ArrayList<>();

    public void add(Widget widget) { add(widget, false); }
    public void add(Widget widget, boolean queued)
    {
        if (queued) queuedWidgets.add(widget);
        else widgets.add(widget);
    }
    public void remove(Widget widget) { remove(widget, false); }
    public void remove(Widget widget, boolean queued)
    {
        if (queued) queuedWidgets.remove(widget);
        else widgets.remove(widget);
    }

    public void bake()
    {
        if (widgets.size() == 0 && queuedWidgets.size() == 0)
        {
            x = 0;
            y = 0;
            width = 0;
            height = 0;
            return;
        }

        x = Integer.MAX_VALUE;
        y = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Widget widget : widgets)
        {
            x = Math.min(x, widget.x);
            y = Math.min(y, widget.y);
            maxX = Math.max(maxX, widget.x + widget.getWidth());
            maxY = Math.max(maxY, widget.y + widget.getHeight());
        }
        for (Widget widget : queuedWidgets)
        {
            x = Math.min(x, widget.x);
            y = Math.min(y, widget.y);
            maxX = Math.max(maxX, widget.x + widget.getWidth());
            maxY = Math.max(maxY, widget.y + widget.getHeight());
        }

        width = maxX - x;
        height = maxY - y;
    }
    public void offset(int x, int y)
    {
        this.x += x;
        this.y += y;
        for (Widget widget : widgets)
        {
            widget.x += x;
            widget.y += y;
        }
        for (Widget widget : queuedWidgets)
        {
            widget.x += x;
            widget.y += y;
        }
    }
    public void addWidgets(Consumer<Widget> consumer)
    {
        widgets.forEach(consumer);
    }
    public void addQueuedWidgets(Consumer<Widget> consumer)
    {
        queuedWidgets.forEach(consumer);
    }

    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {
        return height;
    }
}
