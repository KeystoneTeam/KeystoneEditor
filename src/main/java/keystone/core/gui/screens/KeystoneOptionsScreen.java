package keystone.core.gui.screens;

import keystone.core.KeystoneConfig;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class KeystoneOptionsScreen extends Screen
{
    private static final int PADDING = 5;
    
    private final Screen parent;
    private Viewport optionsViewport;

    private FieldWidgetList optionsWidgets;
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
        
        optionsWidgets = new FieldWidgetList(this, getTitle(), () -> null, KeystoneConfig.class, optionsViewport.getMinX(), optionsViewport.getMinY() + PADDING, optionsViewport.getWidth(), optionsViewport.getHeight() - PADDING, PADDING);
        optionsWidgets.bake();
        
        int buttonY = optionsViewport.getMaxY() + 10;
        doneButton = addDrawableChild(new ButtonNoHotkey(optionsViewport.getMinX(), buttonY, optionsViewport.getWidth() / 2 - 2, 20, ScreenTexts.DONE, this::doneButton));
        resetButton = addDrawableChild(new ButtonNoHotkey(optionsViewport.getMinX() + doneButton.getWidth() + 4, buttonY, doneButton.getWidth(), 20, Text.translatable("keystone.options.reset"), this::resetButton));
        
        addDrawableChild(optionsWidgets);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        // Draw Background
        renderDarkening(context);
        context.fill(0, optionsViewport.getMinY(), width, optionsViewport.getMaxY(), 0x80000000);
        
        // Draw Label
        context.getMatrices().push();
        context.getMatrices().scale(2, 2, 2);
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 4, 6, 0xFFFFFFFF);
        context.getMatrices().pop();
        
        super.render(context, mouseX, mouseY, delta);
    }
    @Override
    public void tick()
    {
        optionsWidgets.tick();
    }

    private void doneButton(ButtonWidget buttonWidget)
    {
        close();
        KeystoneConfig.save();
    }
    private void resetButton(ButtonWidget buttonWidget)
    {
        // TODO: Handle Config Resetting
        init(client, width, height);
    }
}
