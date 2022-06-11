package keystone.core.gui.screens;

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

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, viewport.getMinX(), viewport.getMinY(), viewport.getMaxX(), viewport.getMaxY(), 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public final void setupViewport() { this.viewport = createViewport(); }
    public final Viewport getViewport() { return viewport; }
}
