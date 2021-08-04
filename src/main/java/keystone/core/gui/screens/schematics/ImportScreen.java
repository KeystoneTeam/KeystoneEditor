package keystone.core.gui.screens.schematics;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImportScreen extends KeystoneOverlay
{
    private static final int MARGINS = 2;
    private static final int PADDING = 5;
    private static final int OPTIONS_PADDING = 5;
    private static final int BUTTON_HEIGHT = 14;
    private static final double tooltipWidth = 0.2;

    private static ImportScreen open;

    private final ImportModule importModule;
    private int panelMinY;
    private int panelMaxY;
    private int panelWidth;

    private NudgeButton nudgeImports;
    private Map<ResourceLocation, Boolean> extensionsToPlace;

    protected ImportScreen()
    {
        super(new TranslationTextComponent("keystone.screen.import"));
        importModule = Keystone.getModule(ImportModule.class);

        extensionsToPlace = new HashMap<>();
        for (ImportBoundingBox importBox : importModule.getImportBoxes())
        {
            for (ResourceLocation extension : importBox.getSchematic().getExtensionIDs())
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

    //region Static Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        if (event.slot == KeystoneHotbarSlot.IMPORT && Keystone.getModule(ImportModule.class).getImportBoxes().size() > 0) open();
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
        int widgetsHeight = (3 * (BUTTON_HEIGHT + PADDING)) + OPTIONS_PADDING + IntegerWidget.getFinalHeight() + OPTIONS_PADDING;
        widgetsHeight += extensionsToPlace.size() * (20 + PADDING);
        int y = (height - widgetsHeight) / 2;
        panelMinY = y - MARGINS;
        panelMaxY = panelMinY + widgetsHeight + MARGINS + MARGINS;

        SimpleButton rotateButton = createButton(y, "keystone.schematic_import.rotate", this::rotateButton);
        SimpleButton mirrorButton = createButton(y, "keystone.schematic_import.mirror", this::mirrorButton);
        rotateButton.setWidth(Math.max(rotateButton.getWidth(), mirrorButton.getWidth()));
        mirrorButton.setWidth(Math.max(rotateButton.getWidth(), mirrorButton.getWidth()));
        mirrorButton.x += rotateButton.getWidth() + PADDING;
        panelWidth = 2 * (PADDING + rotateButton.getWidth()) - PADDING + 2 * MARGINS;
        y += BUTTON_HEIGHT + PADDING;

        nudgeImports = createNudgeButton(y, this::nudgeButton);
        nudgeImports.x = (panelWidth - nudgeImports.getWidth()) / 2;
        y += BUTTON_HEIGHT + PADDING + OPTIONS_PADDING;

        addButton(rotateButton);
        addButton(mirrorButton);
        addButton(nudgeImports);

        TranslationTextComponent scaleLabel = new TranslationTextComponent("keystone.schematic_import.scale");
        IntegerWidget scale = new IntegerWidget(scaleLabel, MARGINS, y, panelWidth - 2 * MARGINS, 1, 1, 8)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.setScaleAll(value);
                return true;
            }
        };
        y += scale.getHeight() + PADDING;
        addButton(scale);

        List<CheckboxButton> extensionOptions = new ArrayList<>();
        int defaultPanelWidth = panelWidth;
        for (ResourceLocation extension : extensionsToPlace.keySet())
        {
            CheckboxButton extensionOption = createExtensionOption(y, extension);
            int width = 2 * MARGINS + 24 + font.width(extensionOption.getMessage());
            if (width > panelWidth) panelWidth = width;
            y += extensionOption.getHeight() + PADDING;
            extensionOptions.add(extensionOption);
        }

        rotateButton.x += (panelWidth - defaultPanelWidth) / 2;
        mirrorButton.x += (panelWidth - defaultPanelWidth) / 2;
        nudgeImports.x += (panelWidth - defaultPanelWidth) / 2;
        scale.x += (panelWidth - defaultPanelWidth) / 2;
        for (CheckboxButton extensionOption : extensionOptions)
        {
            extensionOption.setWidth(panelWidth - 2);
            addButton(extensionOption);
        }

        y += OPTIONS_PADDING;
        SimpleButton importButton = createButton(y, "keystone.schematic_import.import", this::importButton);
        importButton.x = (panelWidth - importButton.getWidth()) / 2;
        addButton(importButton);
    }

    @Override
    public void tick()
    {
        nudgeImports.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, 0, panelMinY, panelWidth, panelMaxY, 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
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
            importModule.clearImportBoxes(true);
            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }
    //endregion
    //region Helpers
    private NudgeButton createNudgeButton(int y, NudgeButton.NudgeConsumer consumer)
    {
        int buttonWidth = 2 * PADDING + font.width(NudgeButton.NUDGE.getString());
        return (NudgeButton) new NudgeButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, consumer, NudgeButton.IMPORT_HISTORY_SUPPLIER)
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
    private SimpleButton createButton(int y, String translationKey, Button.IPressable pressable)
    {
        TranslationTextComponent label = new TranslationTextComponent(translationKey);
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(new TranslationTextComponent(translationKey + ".tooltip"));

        int buttonWidth = 2 * PADDING + font.width(label.getString());
        return new SimpleButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, (stack, mouseX, mouseY, partialTicks) -> GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, (int)(tooltipWidth * width), font));
    }
    private CheckboxButton createExtensionOption(int y, ResourceLocation extensionID)
    {
        TranslationTextComponent label = new TranslationTextComponent(extensionID.getNamespace() + "." + extensionID.getPath() + ".shouldPlace");
        return new CheckboxButton(MARGINS, y, panelWidth - 2 * MARGINS, 20, label, extensionsToPlace.get(extensionID), true)
        {
            @Override
            public void onPress()
            {
                super.onPress();
                extensionsToPlace.put(extensionID, selected());
            }
        };
    }
    //endregion
    //region Button Callbacks
    private void rotateButton(Button button)
    {
        importModule.rotateAll();
    }
    private void mirrorButton(Button button)
    {
        importModule.mirrorAll();
    }
    private void nudgeButton(Direction direction, int amount)
    {
        importModule.nudgeAll(direction, amount);
    }
    private void importButton(Button button)
    {
        importModule.placeAll(extensionsToPlace);
    }
    //endregion
}
