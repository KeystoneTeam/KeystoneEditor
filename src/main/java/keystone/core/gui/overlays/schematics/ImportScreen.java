package keystone.core.gui.overlays.schematics;

import keystone.api.Keystone;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.KeystonePanel;
import keystone.core.gui.overlays.hotbar.KeystoneHotbar;
import keystone.core.gui.overlays.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.modules.schematic_import.ImportBoundingBox;
import keystone.core.modules.schematic_import.ImportModule;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportScreen extends KeystonePanel
{
    private static final int MARGINS = 2;
    private static final int PADDING = 5;
    private static final int OPTIONS_PADDING = 5;
    private static final int BUTTON_HEIGHT = 14;
    private static final double tooltipWidth = 0.2;

    private static ImportScreen open;

    private final ImportModule importModule;
    private NudgeButton nudgeImports;
    private Map<Identifier, Boolean> extensionsToPlace;

    protected ImportScreen()
    {
        super(Text.literal("keystone.screen.import"));
        importModule = Keystone.getModule(ImportModule.class);

        extensionsToPlace = new HashMap<>();
        for (ImportBoundingBox importBox : importModule.getImportBoxes())
        {
            for (Identifier extension : importBox.getSchematic().getExtensionIDs())
            {
                if (!extensionsToPlace.containsKey(extension)) extensionsToPlace.put(extension, importBox.getSchematic().getExtension(extension).placeByDefault());
            }
        }
    }
    public static void open()
    {
        if (open == null)
        {
            open = new ImportScreen();
            KeystoneOverlayHandler.addOverlay(open);
        }
    }
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(ImportScreen::onHotbarChanged);
    }

    //region Static Event Handlers
    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot == KeystoneHotbarSlot.IMPORT && Keystone.getModule(ImportModule.class).getImportBoxes().size() > 0) open();
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

        int widgetsHeight = (3 * (BUTTON_HEIGHT + PADDING)) + OPTIONS_PADDING + IntegerWidget.getFinalHeight() + OPTIONS_PADDING;
        widgetsHeight += extensionsToPlace.size() * (20 + PADDING);

        return dock.createLeftCenteredViewport(widgetsHeight + 2 * MARGINS);
    }
    @Override
    protected void setupPanel()
    {
        int x = getViewport().getMinX() + MARGINS;
        int y = getViewport().getMinY() + MARGINS;

        SimpleButton rotateButton = createButton(y, "keystone.schematic_import.rotate", this::rotateButton);
        SimpleButton mirrorButton = createButton(y, "keystone.schematic_import.mirror", this::mirrorButton);
        rotateButton.setWidth(Math.max(rotateButton.getWidth(), mirrorButton.getWidth()));
        mirrorButton.setWidth(Math.max(rotateButton.getWidth(), mirrorButton.getWidth()));
        mirrorButton.x += rotateButton.getWidth() + PADDING;
        int idealWidth = 2 * (PADDING + rotateButton.getWidth()) - PADDING + 2 * MARGINS;
        y += BUTTON_HEIGHT + PADDING;

        nudgeImports = createNudgeButton(y, this::nudgeButton);
        nudgeImports.x = (idealWidth - nudgeImports.getWidth()) / 2;
        y += BUTTON_HEIGHT + PADDING + OPTIONS_PADDING;

        addDrawableChild(rotateButton);
        addDrawableChild(mirrorButton);
        addDrawableChild(nudgeImports);

        IntegerWidget scale = new IntegerWidget(Text.translatable("keystone.schematic_import.scale"), x, y, getViewport().getWidth() - 2 * MARGINS, 1, 1, 8)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.setScaleAll(value);
                return true;
            }
        };
        y += scale.getHeight() + PADDING;
        addDrawableChild(scale);

        List<CheckboxWidget> extensionOptions = new ArrayList<>();
        int defaultPanelWidth = getViewport().getWidth();
        for (Identifier extension : extensionsToPlace.keySet())
        {
            CheckboxWidget extensionOption = createExtensionOption(y, extension);
            y += extensionOption.getHeight() + PADDING;
            extensionOptions.add(extensionOption);
        }

        rotateButton.x += (getViewport().getWidth() - idealWidth) / 2;
        mirrorButton.x += (getViewport().getWidth() - idealWidth) / 2;
        nudgeImports.x += (getViewport().getWidth() - idealWidth) / 2;
        for (CheckboxWidget extensionOption : extensionOptions)
        {
            extensionOption.setWidth(getViewport().getWidth() - 2);
            addDrawableChild(extensionOption);
        }

        y += OPTIONS_PADDING;
        SimpleButton importButton = createButton(y, "keystone.schematic_import.import", this::importButton);
        importButton.x = (getViewport().getWidth() - importButton.getWidth()) / 2;
        addDrawableChild(importButton);
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fillPanel(matrixStack, 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    @Override
    public void tick()
    {
        nudgeImports.tick();
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            importButton(null);
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_R)
        {
            importModule.rotateAll();
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_M)
        {
            importModule.mirrorAll();
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            importModule.clearImportBoxes(true, true);
            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }
    //endregion
    //region Helpers
    private NudgeButton createNudgeButton(int y, NudgeButton.NudgeConsumer consumer)
    {
        int buttonWidth = 2 * PADDING + textRenderer.getWidth(NudgeButton.NUDGE.getString());
        return (NudgeButton) new NudgeButton(getViewport().getMinX() + MARGINS, y, buttonWidth, BUTTON_HEIGHT, consumer, NudgeButton.IMPORT_HISTORY_CALLBACK)
        {
            @Override
            protected int getNudgeStep(Direction direction, int button)
            {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) return 1;
                else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) return -1;
                else return 0;
            }
        }.setColors(0x80008000, 0x80008000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808080);
    }
    private SimpleButton createButton(int y, String translationKey, ButtonWidget.PressAction pressable)
    {
        Text label = Text.translatable(translationKey);
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.translatable(translationKey + ".tooltip"));

        int buttonWidth = 2 * PADDING + textRenderer.getWidth(label.getString());
        return new SimpleButton(getViewport().getMinX() + MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, (stack, mouseX, mouseY, partialTicks) -> renderTooltip(stack, tooltip, mouseX, mouseY));
    }
    private CheckboxWidget createExtensionOption(int y, Identifier extensionID)
    {
        Text label = Text.translatable(extensionID.getNamespace() + "." + extensionID.getPath() + ".shouldPlace");
        return new CheckboxWidget(getViewport().getMinX() + MARGINS, y, getViewport().getWidth() - 2 * MARGINS, 20, label, extensionsToPlace.get(extensionID), true)
        {
            @Override
            public void onPress()
            {
                super.onPress();
                extensionsToPlace.put(extensionID, isChecked());
            }
        };
    }
    //endregion
    //region Button Callbacks
    private void rotateButton(ButtonWidget button)
    {
        importModule.rotateAll();
    }
    private void mirrorButton(ButtonWidget button)
    {
        importModule.mirrorAll();
    }
    private void nudgeButton(Direction direction, int amount)
    {
        importModule.nudgeAll(direction, amount);
    }
    private void importButton(ButtonWidget button)
    {
        importModule.placeAll(extensionsToPlace, true, true, true);
    }
    //endregion
}