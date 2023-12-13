package keystone.core.gui.overlays.schematics;

import keystone.api.Keystone;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.overlays.KeystonePanel;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.gui.widgets.inputs.BooleanWidget;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.modules.hotkeys.HotkeySet;
import keystone.core.modules.schematic_import.ImportBoundingBox;
import keystone.core.modules.schematic_import.ImportModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
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
    private static ImportScreen INSTANCE;
    
    private static final int MARGINS = 2;
    private static final int PADDING = 5;
    private static final int OPTIONS_PADDING = 5;
    private static final int BUTTON_HEIGHT = 14;
    
    private final ImportModule importModule;
    private final Map<Identifier, Boolean> extensionsToPlace;
    
    private NudgeButton nudgeImports;
    private BooleanWidget copyAir;
    
    private ImportScreen()
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
        if (INSTANCE == null) INSTANCE = new ImportScreen();
        KeystoneOverlayHandler.addUniqueOverlay(INSTANCE);
    }
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(ImportScreen::onHotbarChanged);
    }

    //region Static Event Handlers
    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot == KeystoneHotbarSlot.IMPORT && Keystone.getModule(ImportModule.class).getImportBoxes().size() > 0) open();
        else if (INSTANCE != null) INSTANCE.close();
    }
    //endregion
    //region Screen Overrides
    @Override
    protected Viewport createViewport()
    {
        Viewport dock = ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.LEFT, Viewport.MIDDLE, Viewport.LEFT);
        
        int widgetsHeight = (3 * (BUTTON_HEIGHT + PADDING)) + OPTIONS_PADDING + IntegerWidget.getFinalHeight() + OPTIONS_PADDING;
        widgetsHeight += (extensionsToPlace.size() + 1) * (20 + PADDING);

        return dock.setHeight(widgetsHeight + 2 * MARGINS);
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
        mirrorButton.setX(mirrorButton.getX() + rotateButton.getWidth() + PADDING);
        int idealWidth = 2 * (PADDING + rotateButton.getWidth()) - PADDING + 2 * MARGINS;
        y += BUTTON_HEIGHT + PADDING;

        nudgeImports = createNudgeButton(y, this::nudgeButton);
        nudgeImports.setX((idealWidth - nudgeImports.getWidth()) / 2);
        y += BUTTON_HEIGHT + PADDING + OPTIONS_PADDING;

        addDrawableChild(rotateButton);
        addDrawableChild(mirrorButton);
        addDrawableChild(nudgeImports);

        // Scale Widget
        IntegerWidget scale = (IntegerWidget) new IntegerWidget(Text.translatable("keystone.schematic_import.scale"), x, y, getViewport().getWidth() - 2 * MARGINS, 1, 1, 8)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.setScaleAll(value);
                return true;
            }
        }.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.schematic_import.scale.tooltip")));
        y += scale.getHeight() + PADDING;
        addDrawableChild(scale);
    
        // Copy Air Field
        copyAir = new BooleanWidget(x, y, getViewport().getWidth() - 2 * MARGINS, 20, Text.translatable("keystone.clone.copyAir"), true);
        copyAir.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.clone.copyAir.tooltip")));
        y += copyAir.getHeight() + PADDING;
        addDrawableChild(copyAir);

        // Extension Options
        List<CheckboxWidget> extensionOptions = new ArrayList<>();
        int defaultPanelWidth = getViewport().getWidth();
        for (Identifier extension : extensionsToPlace.keySet())
        {
            CheckboxWidget extensionOption = createExtensionOption(y, extension);
            y += extensionOption.getHeight() + PADDING;
            extensionOptions.add(extensionOption);
        }

        rotateButton.setX(rotateButton.getX() + (getViewport().getWidth() - idealWidth) / 2);
        mirrorButton.setX(mirrorButton.getX() + (getViewport().getWidth() - idealWidth) / 2);
        nudgeImports.setX(nudgeImports.getX() + (getViewport().getWidth() - idealWidth) / 2);
        for (CheckboxWidget extensionOption : extensionOptions)
        {
            extensionOption.setWidth(getViewport().getWidth() - 2);
            addDrawableChild(extensionOption);
        }

        y += OPTIONS_PADDING;
        SimpleButton importButton = createButton(y, "keystone.schematic_import.import", this::importButton);
        importButton.setX((getViewport().getWidth() - importButton.getWidth()) / 2);
        addDrawableChild(importButton);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        fillPanel(context, 0x80000000);
        super.render(context, mouseX, mouseY, partialTicks);
    }
    @Override
    public void tick()
    {
        nudgeImports.tick();
    }
    //endregion
    //region Hotkeys
    @Override
    public HotkeySet getHotkeySet()
    {
        HotkeySet hotkeySet = new HotkeySet("import_mode");
        hotkeySet.getHotkey(GLFW.GLFW_KEY_ENTER).addListener(() -> importButton(null));
        hotkeySet.getHotkey(GLFW.GLFW_KEY_R).addListener(importModule::rotateAll);
        hotkeySet.getHotkey(GLFW.GLFW_KEY_M).addListener(importModule::mirrorAll);
        hotkeySet.getHotkey(GLFW.GLFW_KEY_ESCAPE).clear().addListener(this::cancelHotkey);
        return hotkeySet;
    }
    private void cancelHotkey()
    {
        importModule.clearImportBoxes(true, true);
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
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
        Text tooltip = Text.translatable(translationKey + ".tooltip");

        int buttonWidth = 2 * PADDING + textRenderer.getWidth(label.getString());
        return new SimpleButton(getViewport().getMinX() + MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, IKeystoneTooltip.createSimple(tooltip));
    }
    private CheckboxWidget createExtensionOption(int y, Identifier extensionID)
    {
        String translationKey = extensionID.getNamespace() + "." + extensionID.getPath();
        return new BooleanWidget(getViewport().getMinX() + MARGINS, y, getViewport().getWidth() - 2 * MARGINS, 20, Text.translatable(translationKey + ".shouldPlace"), extensionsToPlace.get(extensionID))
        {
            @Override
            public void onPress()
            {
                super.onPress();
                extensionsToPlace.put(extensionID, isChecked());
            }
        }.setTooltip(IKeystoneTooltip.createSimple(Text.translatable(translationKey + ".tooltip")));
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
        // TODO: Add copyAir checkbox similar to CloneScreen
        importModule.placeAll(extensionsToPlace, copyAir.isChecked(), true, true);
    }
    //endregion
}
