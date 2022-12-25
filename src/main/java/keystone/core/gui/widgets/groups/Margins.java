package keystone.core.gui.widgets.groups;

public record Margins(int left, int right, int top, int bottom)
{
    public Margins(int margin)
    {
        this(margin, margin, margin, margin);
    }
    public Margins(int horizontal, int vertical)
    {
        this(horizontal, horizontal, vertical, vertical);
    }
}
