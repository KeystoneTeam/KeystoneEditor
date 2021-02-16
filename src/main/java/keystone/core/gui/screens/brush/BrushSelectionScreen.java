package keystone.core.gui.screens.brush;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.gui.widgets.inputs.ParsableTextFieldWidget;
import keystone.core.modules.brush.BrushModule;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BrushSelectionScreen extends KeystoneOverlay
{
    public static final int PADDING = 5;
    private static BrushSelectionScreen open;
    private static BrushModule brushModule;

    private int panelMinY;
    private int panelMaxX;
    private int panelMaxY;
    private int totalVariableHeight;

    private List<TextFieldWidget> textFields = new ArrayList<>();

    protected BrushSelectionScreen()
    {
        super(new TranslationTextComponent("keystone.screen.brushPanel"));
    }
    public static void open()
    {
        if (open == null)
        {
            open = new BrushSelectionScreen();
            KeystoneOverlayHandler.addOverlay(open);
        }
    }

    //region Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.slot == KeystoneHotbarSlot.BRUSH) open();
        else if (open != null) open.closeScreen();
    }
    //endregion
    //region Screen Overrides
    @Override
    public void onClose()
    {
        open = null;
    }

    @Override
    protected void init()
    {
        textFields.clear();
        brushModule = Keystone.getModule(BrushModule.class);
        recalculateVariablesHeight();

        int centerHeight = height / 2;
        int halfPanelHeight = (PADDING + 2 * (20 + PADDING) + IntegerWidget.getHeight() + totalVariableHeight) / 2;
        panelMinY = centerHeight - halfPanelHeight;
        panelMaxX = KeystoneHotbar.getX() - 5;
        panelMaxY = centerHeight + halfPanelHeight;

        int y = panelMinY + PADDING;

        // Change Operation Button
        addButton(new ButtonNoHotkey(PADDING, y, panelMaxX - 2 * PADDING, 20, brushModule.getBrushOperation().getName(), (button) ->
        {
            brushModule.setBrushOperation(brushModule.getBrushOperation().getNextOperation());
            init(minecraft, width, height);
        }));
        y += 20 + PADDING;

        // Change Shape Button
        addButton(new ButtonNoHotkey(PADDING, y, panelMaxX - 2 * PADDING, 20, brushModule.getBrushShape().getName(), (button) ->
        {
            brushModule.setBrushShape(brushModule.getBrushShape().getNextShape());
            init(minecraft, width, height);
        }));
        y += 20 + PADDING;

        // Size Fields
        int sizeDimensionWidth = (panelMaxX - PADDING) / 3 - PADDING;
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.width"), PADDING, y, sizeDimensionWidth, brushModule.getBrushSize()[0])
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(value, size[1], size[2]);
                return true;
            }
        });
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.length"), PADDING + sizeDimensionWidth + PADDING, y, sizeDimensionWidth, brushModule.getBrushSize()[2])
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(size[0], size[1], value);
                return true;
            }
        });
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.height"), PADDING + 2 * (sizeDimensionWidth + PADDING), y, sizeDimensionWidth, brushModule.getBrushSize()[1])
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(size[0], value, size[2]);
                return true;
            }
        });
        y += ParsableTextFieldWidget.getHeight() + PADDING;

        rebuildVariables(y);
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        fill(matrixStack, 0, panelMinY, panelMaxX, panelMaxY, 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
    //endregion
    //region Helpers
    private void recalculateVariablesHeight()
    {
        totalVariableHeight = PADDING;
    }
    private void rebuildVariables(int y)
    {
        
    }
    //endregion
}
