package keystone.core.gui.screens.brush;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import keystone.core.modules.brush.BrushModule;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BrushSelectionScreen extends KeystoneOverlay
{
    public static final int PADDING = 5;
    private static BrushSelectionScreen open;
    private static BrushModule brushModule;

    private int panelMinY;
    private int panelMaxX;
    private int panelMaxY;

    private ITextComponent immediateMode;
    private ITextComponent deferredMode;

    private FieldWidgetList brushVariablesList;

    protected BrushSelectionScreen()
    {
        super(new TranslationTextComponent("keystone.screen.brushPanel"));
        immediateMode = new StringTextComponent("I");
        deferredMode = new StringTextComponent("D");
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
    public static void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.slot == KeystoneHotbarSlot.BRUSH) open();
        else if (open != null) open.onClose();
    }
    //endregion
    //region Screen Overrides
    @Override
    public void removed()
    {
        open = null;
    }

    @Override
    protected void init()
    {
        brushModule = Keystone.getModule(BrushModule.class);

        // Calculate panel size
        panelMaxX = Math.min(KeystoneHotbar.getX() - 5, 280);
        brushVariablesList = new FieldWidgetList(brushModule::getBrushOperation, PADDING, panelMaxX - 2 * PADDING, this::disableWidgets, this::restoreWidgets);
        brushVariablesList.bake();
        int centerHeight = height / 2;
        int halfPanelHeight = (PADDING + 2 * (20 + PADDING) + 2 * (IntegerWidget.getFinalHeight() + PADDING) + PADDING + brushVariablesList.getHeight()) / 2;
        panelMinY = centerHeight - halfPanelHeight;
        panelMaxY = centerHeight + halfPanelHeight;

        int y = panelMinY + PADDING;

        // Change Operation Button and Toggle Immediate Mode Button
        addButton(new ButtonNoHotkey(PADDING, y, panelMaxX - 2 * PADDING - 20, 20, brushModule.getBrushOperation().getName(), (button) ->
        {
            brushModule.setBrushOperation(brushModule.getBrushOperation().getNextOperation());
            init(minecraft, width, height);
        }));
        addButton(new ButtonNoHotkey(panelMaxX - 20 - PADDING, y, 20, 20, brushModule.isImmediateMode() ? immediateMode : deferredMode, (button) ->
        {
            brushModule.setImmediateMode(!brushModule.isImmediateMode());
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
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.width"), PADDING, y, sizeDimensionWidth, brushModule.getBrushSize()[0], 1, KeystoneConfig.maxBrushSize)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(value, size[1], size[2]);
                return true;
            }
        });
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.length"), PADDING + sizeDimensionWidth + PADDING, y, sizeDimensionWidth, brushModule.getBrushSize()[2], 1, KeystoneConfig.maxBrushSize)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(size[0], size[1], value);
                return true;
            }
        });
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.height"), PADDING + 2 * (sizeDimensionWidth + PADDING), y, sizeDimensionWidth, brushModule.getBrushSize()[1], 1, KeystoneConfig.maxBrushSize)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(size[0], value, size[2]);
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Spacing and Noise
        int spacingNoiseWidth = (panelMaxX - PADDING) / 2 - PADDING;
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.brush.minimumSpacing"), PADDING, y, spacingNoiseWidth, brushModule.getMinSpacing(), 1, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                brushModule.setMinSpacing(value);
                return true;
            }
        });
        addButton(new IntegerWidget(new TranslationTextComponent("keystone.brush.noise"), PADDING + spacingNoiseWidth + PADDING, y, spacingNoiseWidth, brushModule.getNoise(), 1, 100)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                brushModule.setNoise(value);
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Brush variables
        brushVariablesList.offset(PADDING, y);
        brushVariablesList.addWidgets(this::addButton);
        brushVariablesList.addQueuedWidgets(this::addButton);

        brushModule.getBrushOperation().undirtyEditor();
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (brushModule.getBrushOperation().isEditorDirtied()) init(minecraft, width, height);

        fill(matrixStack, 0, panelMinY, panelMaxX, panelMaxY, 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    //endregion
}
