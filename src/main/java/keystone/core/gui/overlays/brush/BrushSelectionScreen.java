package keystone.core.gui.overlays.brush;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.overlays.KeystonePanel;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.groups.Margins;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import keystone.core.modules.brush.BrushModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class BrushSelectionScreen extends KeystonePanel
{
    public static final int PADDING = 5;
    private static BrushSelectionScreen open;
    private static BrushModule brushModule;

    private Text immediateMode;
    private Text deferredMode;

    private FieldWidgetList brushVariablesList;

    protected BrushSelectionScreen()
    {
        super(Text.translatable("keystone.screen.brushPanel"), true);
        immediateMode = Text.literal("I");
        deferredMode = Text.literal("D");
    }
    public static void open()
    {
        if (open == null)
        {
            open = new BrushSelectionScreen();
            KeystoneOverlayHandler.addOverlay(open);
        }
    }
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(BrushSelectionScreen::onHotbarChanged);
    }

    //region Event Handlers
    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot == KeystoneHotbarSlot.BRUSH) open();
        else if (open != null) open.close();
    }
    //endregion
    //region Screen Overrides
    @Override
    public void removed()
    {
        open = null;
    }

    @Override
    protected Viewport createViewport()
    {
        Viewport dock = ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.LEFT, Viewport.MIDDLE, Viewport.LEFT);

        // Calculate panel size
        brushModule = Keystone.getModule(BrushModule.class);
        int maxPanelHeight = client.getWindow().getScaledHeight() - (PADDING + 2 * (20 + PADDING) + 2 * (IntegerWidget.getFinalHeight() + PADDING) + PADDING);
        brushVariablesList = new FieldWidgetList(this, Text.translatable("keystone.brush.brushVariables"), brushModule::getBrushOperation, 0, 0, dock.getWidth(), maxPanelHeight, PADDING);
        brushVariablesList.bake();
        int panelHeight = PADDING + 2 * (20 + PADDING) + 2 * (IntegerWidget.getFinalHeight() + PADDING) + PADDING + brushVariablesList.getHeight();

        return dock.setHeight(panelHeight);
    }

    @Override
    protected void setupPanel()
    {
        int y = getViewport().getMinY() + PADDING;

        // Change Operation Button and Toggle Immediate Mode Button
        addDrawableChild(new ButtonNoHotkey(PADDING, y, getViewport().getWidth() - 2 * PADDING - 20, 20, brushModule.getBrushOperation().getName(), (button) ->
        {
            brushModule.setBrushOperation(brushModule.getBrushOperation().getNextOperation());
            init(client, width, height);
        }));
        addDrawableChild(new ButtonNoHotkey(getViewport().getMaxX() - 20 - PADDING, y, 20, 20, brushModule.isImmediateMode() ? immediateMode : deferredMode, (button) ->
        {
            brushModule.setImmediateMode(!brushModule.isImmediateMode());
            init(client, width, height);
        }).setTooltip(IKeystoneTooltip.createSimple(brushModule.isImmediateMode() ? Text.translatable("keystone.brush.immediateMode.tooltip") : Text.translatable("keystone.brush.deferredMode.tooltip"))));
        y += 20 + PADDING;

        // Change Shape Button
        addDrawableChild(new ButtonNoHotkey(PADDING, y, getViewport().getWidth() - 2 * PADDING, 20, brushModule.getBrushShape().getName(), (button) ->
        {
            brushModule.setBrushShape(brushModule.getBrushShape().getNextShape());
            init(client, width, height);
        }));
        y += 20 + PADDING;

        // Size Fields
        int sizeDimensionWidth = (getViewport().getWidth() - PADDING) / 3 - PADDING;
        addDrawableChild(new IntegerWidget(Text.translatable("keystone.width"), PADDING, y, sizeDimensionWidth, brushModule.getBrushSize()[0], 1, KeystoneConfig.maxBrushSize)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(value, size[1], size[2]);
                return true;
            }
        });
        addDrawableChild(new IntegerWidget(Text.translatable("keystone.length"), PADDING + sizeDimensionWidth + PADDING, y, sizeDimensionWidth, brushModule.getBrushSize()[2], 1, KeystoneConfig.maxBrushSize)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                int[] size = brushModule.getBrushSize();
                brushModule.setBrushSize(size[0], size[1], value);
                return true;
            }
        });
        addDrawableChild(new IntegerWidget(Text.translatable("keystone.height"), PADDING + 2 * (sizeDimensionWidth + PADDING), y, sizeDimensionWidth, brushModule.getBrushSize()[1], 1, KeystoneConfig.maxBrushSize)
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
        int spacingNoiseWidth = (getViewport().getWidth() - PADDING) / 2 - PADDING;
        addDrawableChild(new IntegerWidget(Text.translatable("keystone.brush.minimumSpacing"), PADDING, y, spacingNoiseWidth, brushModule.getMinSpacing(), 1, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                brushModule.setMinSpacing(value);
                return true;
            }
        }.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.brush.minimumSpacing.tooltip"))));
        addDrawableChild(new IntegerWidget(Text.translatable("keystone.brush.noise"), PADDING + spacingNoiseWidth + PADDING, y, spacingNoiseWidth, brushModule.getNoise(), 1, 100)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                brushModule.setNoise(value);
                return true;
            }
        }.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.brush.noise.tooltip"))));
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Brush variables
        brushVariablesList.move(0, y);
        brushVariablesList.setMargins(new Margins(5, 0));
        addDrawableChild(brushVariablesList);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        fillPanel(context, 0x80000000);
        super.render(context, mouseX, mouseY, partialTicks);
    }
    //endregion
}
