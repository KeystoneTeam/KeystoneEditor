package keystone.core.gui.screens;

import keystone.core.KeystoneConfig;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class KeystoneOptionsScreen extends Screen
{
    private static final int PADDING = 5;
    
    private final Screen parent;
    private Viewport optionsViewport;
    
    private ButtonNoHotkey resetButton;
    private ButtonNoHotkey doneButton;
    
    public KeystoneOptionsScreen(Screen parent)
    {
        super(Text.translatable("keystone.screen.options"));
        this.parent = parent;
    }
    
    @Override
    public void close()
    {
        client.setScreenAndRender(parent);
    }
    @Override
    protected void init()
    {
        optionsViewport = ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.MIDDLE, Viewport.TOP, Viewport.MIDDLE).offset(0, 40, 0, -40);
        
        FieldWidgetList optionsWidgets = new FieldWidgetList(this, getTitle(), () -> null, KeystoneConfig.class, optionsViewport.getMinX(), optionsViewport.getMinY() + PADDING, optionsViewport.getWidth(), optionsViewport.getHeight() - PADDING, PADDING);
        optionsWidgets.bake();
        addDrawableChild(optionsWidgets);
        
        int buttonY = optionsViewport.getMaxY() + 10;
        doneButton = addDrawableChild(new ButtonNoHotkey(optionsViewport.getMinX(), buttonY, optionsViewport.getWidth() / 2 - 2, 20, ScreenTexts.DONE, this::doneButton));
        resetButton = addDrawableChild(new ButtonNoHotkey(optionsViewport.getMinX() + doneButton.getWidth() + 4, buttonY, doneButton.getWidth(), 20, Text.translatable("keystone.options.reset"), this::resetButton));
    }
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        // Draw Background
        renderBackgroundTexture(0);
        fill(matrices, 0, optionsViewport.getMinY(), width, optionsViewport.getMaxY(), 0x80000000);
        
        // Draw Label
        matrices.push();
        matrices.scale(2, 2, 2);
        drawCenteredText(matrices, textRenderer, getTitle(), width / 4, 6, 0xFFFFFFFF);
        matrices.pop();
        
        super.render(matrices, mouseX, mouseY, delta);
    }
    
    private void doneButton(ButtonWidget buttonWidget)
    {
        close();
    }
    private void resetButton(ButtonWidget buttonWidget)
    {
        init(client, width, height);
    }
}
