package keystone.core.gui.overlays;

import keystone.core.gui.viewports.Viewport;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class KeystonePanel extends KeystoneOverlay
{
    private Viewport viewport;

    public KeystonePanel(Text titleIn)
    {
        super(titleIn);
    }

    protected abstract Viewport createViewport();
    protected void setupPanel() { }

    @Override
    protected void init()
    {
        setupViewport();
        setupPanel();
    }
    
    public final void setupViewport() { this.viewport = createViewport(); }
    public final Viewport getViewport() { return viewport; }
    protected final void fillPanel(MatrixStack matrixStack, int color)
    {
        fill(matrixStack, viewport.getMinX(), viewport.getMinY(), viewport.getMaxX(), viewport.getMaxY(), color);
    }
}
