package keystone.core.gui.overlays;

import keystone.core.gui.viewports.Viewport;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class KeystonePanel extends KeystoneOverlay
{
    private Viewport viewport;
    private boolean viewportBlocksMouse;

    public KeystonePanel(Text titleIn, boolean viewportBlocksMouse)
    {
        super(titleIn);
        this.viewportBlocksMouse  = viewportBlocksMouse;
    }

    protected abstract Viewport createViewport();
    protected void setupPanel() { }

    @Override
    protected void init()
    {
        setupViewport();
        setupPanel();
    }
    @Override
    public boolean isMouseBlocked(double mouseX, double mouseY)
    {
        if (viewportBlocksMouse) return mouseX >= viewport.getMinX() && mouseX <= viewport.getMaxX() && mouseY >= viewport.getMinY() && mouseY <= viewport.getMaxY();
        else return super.isMouseBlocked(mouseX, mouseY);
    }
    
    public final void setupViewport() { this.viewport = createViewport(); }
    public final Viewport getViewport() { return viewport; }
    protected final void fillPanel(MatrixStack matrixStack, int color)
    {
        fill(matrixStack, viewport.getMinX(), viewport.getMinY(), viewport.getMaxX(), viewport.getMaxY(), color);
    }
}
