package keystone.core.gui.screens.schematics;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.KeystoneConfig;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.selection.SelectionNudgeScreen;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.modules.WorldModifierModules;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
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
public class CloneScreen extends KeystoneOverlay
{
    private static final int MARGINS = 2;
    private static final int PADDING = 5;
    private static final int OPTIONS_PADDING = 5;
    private static final int BUTTON_HEIGHT = 14;
    private static final double tooltipWidth = 0.2;

    private static CloneScreen open;

    private final KeystoneSchematic schematic;
    private final ImportModule importModule;
    private int panelMinY;
    private int panelMaxY;
    private int panelWidth;

    private NudgeButton nudgeImports;
    private IntegerWidget offsetX;
    private IntegerWidget offsetY;
    private IntegerWidget offsetZ;

    private Coords anchor;
    private Vector3i offset = new Vector3i(0, 0, 0);
    private int repeat = 1;
    private Map<ResourceLocation, Boolean> extensionsToPlace;

    protected CloneScreen()
    {
        super(new TranslationTextComponent("keystone.screen.clone"));

        SelectionBoundingBox currentSelection = SelectionNudgeScreen.getSelectionToNudge();
        schematic = KeystoneSchematic.createFromSelection(currentSelection, new WorldModifierModules());
        extensionsToPlace = new HashMap<>();
        for (ResourceLocation extension : schematic.getExtensionIDs())
        {
            if (!extensionsToPlace.containsKey(extension)) extensionsToPlace.put(extension, schematic.getExtension(extension).placeByDefault());
        }

        anchor = currentSelection.getMinCoords();
        importModule = Keystone.getModule(ImportModule.class);
        importModule.setCloneImportBoxes(schematic, anchor, offset, repeat);
    }
    public static void open()
    {
        if (open == null)
        {
            open = new CloneScreen();
            KeystoneOverlayHandler.addOverlay(open);
        }
    }

    //region Static Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        if (event.slot == KeystoneHotbarSlot.CLONE) open();
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
        widgetsHeight += 2 * (IntegerWidget.getFinalHeight() + PADDING);
        widgetsHeight += extensionsToPlace.size() * (20 + PADDING);
        int y = (height - widgetsHeight) / 2;
        panelMinY = y - MARGINS;
        panelMaxY = panelMinY + widgetsHeight + MARGINS + MARGINS;

        SimpleButton rotateButton = createButton(y, "keystone.schematic_import.rotate", this::rotateButton);
        SimpleButton mirrorButton = createButton(y, "keystone.schematic_import.mirror", this::mirrorButton);
        rotateButton.setWidth(Math.max(rotateButton.getWidth(), mirrorButton.getWidth()));
        mirrorButton.setWidth(Math.max(rotateButton.getWidth(), mirrorButton.getWidth()));
        mirrorButton.x += rotateButton.getWidth() + PADDING;
        int idealWidth = 2 * (PADDING + rotateButton.getWidth()) - PADDING + 2 * MARGINS;
        panelWidth = Math.min(KeystoneHotbar.getX() - 5, 200);
        y += BUTTON_HEIGHT + PADDING;

        nudgeImports = createNudgeButton(y, this::nudgeButton);
        nudgeImports.x = (idealWidth - nudgeImports.getWidth()) / 2;
        y += BUTTON_HEIGHT + PADDING + OPTIONS_PADDING;

        addButton(rotateButton);
        addButton(mirrorButton);
        addButton(nudgeImports);

        // Offset Fields
        int offsetWidgetWidth = (panelWidth - 2 * (MARGINS + PADDING)) / 3;
        offsetX = addButton(new IntegerWidget(new TranslationTextComponent("keystone.clone.offsetX"), MARGINS, y, offsetWidgetWidth, offset.x, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                offset = new Vector3i(value, offset.y, offset.z);
                importModule.setCloneImportBoxes(schematic, anchor, offset, repeat);
                return true;
            }
        });
        offsetY = addButton(new IntegerWidget(new TranslationTextComponent("keystone.clone.offsetY"), MARGINS + offsetWidgetWidth + PADDING, y, offsetWidgetWidth, offset.y, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                offset = new Vector3i(offset.x, value, offset.z);
                importModule.setCloneImportBoxes(schematic, anchor, offset, repeat);
                return true;
            }
        });
        offsetZ = addButton(new IntegerWidget(new TranslationTextComponent("keystone.clone.offsetZ"), MARGINS + 2 * (offsetWidgetWidth + PADDING), y, offsetWidgetWidth, offset.z, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                offset = new Vector3i(offset.x, offset.y, value);
                importModule.setCloneImportBoxes(schematic, anchor, offset, repeat);
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Repeat Field
        IntegerWidget repeatField = addButton(new IntegerWidget(new TranslationTextComponent("keystone.clone.repeat"), MARGINS, y, idealWidth - 2 * MARGINS, repeat, 1, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                repeat = value;
                importModule.setCloneImportBoxes(schematic, anchor, offset, repeat);
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Scale Field
        TranslationTextComponent scaleLabel = new TranslationTextComponent("keystone.schematic_import.scale");
        IntegerWidget scale = new IntegerWidget(scaleLabel, MARGINS, y, idealWidth - 2 * MARGINS, 1, 1, 8)
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

        for (ResourceLocation extension : extensionsToPlace.keySet())
        {
            CheckboxButton extensionOption = createExtensionOption(y, extension);
            y += extensionOption.getHeight() + PADDING;
            addButton(extensionOption);
        }

        rotateButton.x += (panelWidth - idealWidth) / 2;
        mirrorButton.x += (panelWidth - idealWidth) / 2;
        nudgeImports.x += (panelWidth - idealWidth) / 2;
        repeatField.x += (panelWidth - idealWidth) / 2;
        scale.x += (panelWidth - idealWidth) / 2;

        y += OPTIONS_PADDING;
        SimpleButton cloneButton = createButton(y, "keystone.clone.clone", this::cloneButton);
        cloneButton.x = (panelWidth - cloneButton.getWidth()) / 2;
        addButton(cloneButton);
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
            cloneButton(null);
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
        switch (direction)
        {
            case EAST: offset = new Vector3i(offset.x + amount, offset.y, offset.z); break;
            case WEST: offset = new Vector3i(offset.x - amount, offset.y, offset.z); break;
            case UP: offset = new Vector3i(offset.x, offset.y + amount, offset.z); break;
            case DOWN: offset = new Vector3i(offset.x, offset.y - amount, offset.z); break;
            case SOUTH: offset = new Vector3i(offset.x, offset.y, offset.z + amount); break;
            case NORTH: offset = new Vector3i(offset.x, offset.y, offset.z - amount); break;
        }

        offsetX.setValue(String.valueOf(offset.x));
        offsetY.setValue(String.valueOf(offset.y));
        offsetZ.setValue(String.valueOf(offset.z));
        importModule.setCloneImportBoxes(schematic, anchor, offset, repeat);
    }
    private void cloneButton(Button button)
    {
        importModule.placeAll(extensionsToPlace);
        //TODO: Implement new history entry for clone operations
    }
    //endregion
}
