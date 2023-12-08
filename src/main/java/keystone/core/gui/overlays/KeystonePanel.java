package keystone.core.gui.overlays;

import keystone.core.DebugFlags;
import keystone.core.gui.viewports.Viewport;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public abstract class KeystonePanel extends KeystoneOverlay
{
    private Viewport viewport;
    private boolean viewportBlocksMouse;

    public KeystonePanel(Text titleIn)
    {
        super(titleIn);
        this.viewportBlocksMouse = true;
    }
    
    protected abstract Viewport createViewport();
    protected void setupPanel() { }
    
    public KeystonePanel setViewportBlocksMouse(boolean viewportBlocksMouse)
    {
        this.viewportBlocksMouse = viewportBlocksMouse;
        return this;
    }
    public final boolean viewportBlocksMouse() { return viewportBlocksMouse; }

    @Override
    protected void init()
    {
        setupViewport();
        setupPanel();
    }
    @Override
    public boolean isMouseBlocked(double mouseX, double mouseY)
    {
        if (viewportBlocksMouse)
        {
            mouseX /= viewport.getScale();
            mouseY /= viewport.getScale();
            return mouseX >= viewport.getMinX() && mouseX <= viewport.getMaxX() && mouseY >= viewport.getMinY() && mouseY <= viewport.getMaxY();
        }
        else return super.isMouseBlocked(mouseX, mouseY);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        if (DebugFlags.isFlagSet("debugViewports")) viewport.renderDebug(context);
    }
    
    public final void setupViewport() { this.viewport = createViewport(); }
    public final Viewport getViewport() { return viewport; }
    protected final void fillPanel(DrawContext context, int color)
    {
        context.fill(viewport.getMinX(), viewport.getMinY(), viewport.getMaxX(), viewport.getMaxY(), color);
    }
}
