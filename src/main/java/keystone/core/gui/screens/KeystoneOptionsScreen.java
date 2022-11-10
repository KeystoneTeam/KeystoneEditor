package keystone.core.gui.screens;

import keystone.core.KeystoneConfig;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class KeystoneOptionsScreen extends Screen
{
    private static final int PADDING = 5;
    
    private Viewport optionsViewport;
    private FieldWidgetList optionsWidgets;
    
    public KeystoneOptionsScreen()
    {
        super(Text.translatable("keystone.screen.options"));
    }
    
    @Override
    protected void init()
    {
        Viewport topViewport = ScreenViewports.getViewport(Viewport.TOP, Viewport.MIDDLE);
        optionsViewport = ScreenViewports.getViewport(Viewport.MIDDLE, Viewport.MIDDLE);
        Viewport bottomViewport = ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.MIDDLE);
        
        this.optionsWidgets = new FieldWidgetList(this, getTitle(), () -> null, KeystoneConfig.class, optionsViewport.getMinX(), optionsViewport.getMinY() + PADDING, optionsViewport.getWidth(), optionsViewport.getHeight() - 2 * PADDING, PADDING);
        this.optionsWidgets.bake();
        addDrawableChild(this.optionsWidgets);
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        // Draw Background
        renderBackgroundTexture(0);
        fill(matrices, 0, optionsViewport.getMinY(), width, optionsViewport.getMaxY(), 0x80000000);
        
        super.render(matrices, mouseX, mouseY, delta);
    }
}
