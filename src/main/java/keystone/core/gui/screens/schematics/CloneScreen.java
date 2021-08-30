package keystone.core.gui.screens.schematics;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.screens.selection.SelectionNudgeScreen;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.CloneImportBoxesHistoryEntry;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
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
    private static Coords anchor;
    private static Rotation rotation;
    private static Mirror mirror;
    private static Vector3i offset = new Vector3i(0, 0, 0);
    private static int repeat = 1;
    private static int scale = 1;
    private static Map<ResourceLocation, Boolean> extensionsToPlace;

    private final SelectionBoundingBox selectionBox;
    private final KeystoneSchematic schematic;
    private final ImportModule importModule;
    private int panelMinY;
    private int panelMaxY;
    private int panelWidth;

    private NudgeButton nudgeImports;
    private IntegerWidget offsetX;
    private IntegerWidget offsetY;
    private IntegerWidget offsetZ;
    private IntegerWidget repeatField;
    private IntegerWidget scaleField;

    protected CloneScreen(BoundingBox selectionBounds, KeystoneSchematic schematic, Coords anchor, Rotation rotation, Mirror mirror, Vector3i offset, int repeat, int scale, boolean addHistoryEntry)
    {
        super(new TranslationTextComponent("keystone.screen.clone"));

        importModule = Keystone.getModule(ImportModule.class);
        selectionBox = SelectionBoundingBox.createFromBoundingBox(selectionBounds);
        this.schematic = schematic;
        CloneScreen.anchor = anchor;
        CloneScreen.rotation = rotation;
        CloneScreen.mirror = mirror;
        CloneScreen.offset = offset;
        CloneScreen.repeat = repeat;
        CloneScreen.scale = scale;
        extensionsToPlace = new HashMap<>();
        for (ResourceLocation extension : schematic.getExtensionIDs())
        {
            if (!extensionsToPlace.containsKey(extension)) extensionsToPlace.put(extension, schematic.getExtension(extension).placeByDefault());
        }

        if (addHistoryEntry)
        {
            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(new CloneImportBoxesHistoryEntry(selectionBounds, schematic, anchor, rotation, mirror, offset, repeat, scale, true));
            historyModule.tryEndHistoryEntry();
        }

        importModule.restoreCloneImportBoxes(schematic, anchor, rotation, mirror, offset, repeat, scale);
        importModule.setHistoryEntrySupplier(() -> new CloneImportBoxesHistoryEntry(selectionBox.getBoundingBox(), schematic, anchor, rotation, mirror, offset, repeat, scale, false));
    }
    public static void open()
    {
        if (open == null)
        {
            SelectionBoundingBox selection = SelectionNudgeScreen.getSelectionToNudge();
            open = new CloneScreen(selection.getBoundingBox(), KeystoneSchematic.createFromSelection(selection, new WorldModifierModules()), selection.getMinCoords(), Rotation.NONE, Mirror.NONE, new Vector3i(0, 0, 0), 1, 1, true);
            KeystoneOverlayHandler.addOverlay(open);
        }
    }
    public static void reopen(BoundingBox bounds, KeystoneSchematic schematic, Coords anchor, Rotation rotation, Mirror mirror, Vector3i offset, int repeat, int scale)
    {
        if (open == null)
        {
            open = new CloneScreen(bounds, schematic, anchor, rotation, mirror, offset, repeat, scale, false);
            KeystoneOverlayHandler.addOverlay(open);
            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.CLONE);
        }
        else
        {
            open.offsetX.setTypedValue(offset.x);
            open.offsetY.setTypedValue(offset.y);
            open.offsetZ.setTypedValue(offset.z);
            open.repeatField.setTypedValue(repeat);
            open.scaleField.setTypedValue(scale);
        }
    }

    public static Coords getAnchor() { return anchor; }
    public static Rotation getRotation() { return rotation; }
    public static Mirror getMirror() { return mirror; }
    public static Vector3i getOffset() { return offset; }
    public static int getRepeat() { return repeat; }
    public static int getScale() { return scale; }
    public static Map<ResourceLocation, Boolean> getExtensionsToPlace() { return extensionsToPlace; }
    //region Static Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHotbarChanged(final KeystoneHotbarEvent event)
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
        ImportModule importModule = Keystone.getModule(ImportModule.class);
        importModule.clearImportBoxes(false);
        importModule.setHistoryEntrySupplier(ImportModule.IMPORT_HISTORY_SUPPLIER);
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
                Vector3i newOffset = new Vector3i(value, offset.y, offset.z);
                importModule.setCloneImportBoxes(selectionBox, schematic, anchor, rotation, mirror, newOffset, repeat, scale);
                offset = newOffset;
                return true;
            }
        });
        offsetY = addButton(new IntegerWidget(new TranslationTextComponent("keystone.clone.offsetY"), MARGINS + offsetWidgetWidth + PADDING, y, offsetWidgetWidth, offset.y, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                Vector3i newOffset = new Vector3i(offset.x, value, offset.z);
                importModule.setCloneImportBoxes(selectionBox, schematic, anchor, rotation, mirror, newOffset, repeat, scale);
                offset = newOffset;
                return true;
            }
        });
        offsetZ = addButton(new IntegerWidget(new TranslationTextComponent("keystone.clone.offsetZ"), MARGINS + 2 * (offsetWidgetWidth + PADDING), y, offsetWidgetWidth, offset.z, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                Vector3i newOffset = new Vector3i(offset.x, offset.y, value);
                importModule.setCloneImportBoxes(selectionBox, schematic, anchor, rotation, mirror, newOffset, repeat, scale);
                offset = newOffset;
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Repeat Field
        repeatField = addButton(new IntegerWidget(new TranslationTextComponent("keystone.clone.repeat"), MARGINS, y, idealWidth - 2 * MARGINS, repeat, 1, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.setCloneImportBoxes(selectionBox, schematic, anchor, rotation, mirror, offset, value, scale);
                repeat = value;
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Scale Field
        TranslationTextComponent scaleLabel = new TranslationTextComponent("keystone.schematic_import.scale");
        scaleField = new IntegerWidget(scaleLabel, MARGINS, y, idealWidth - 2 * MARGINS, scale, 1, 8)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.setCloneImportBoxes(selectionBox, schematic, anchor, rotation, mirror, offset, repeat, value);
                scale = value;
                return true;
            }
        };;
        y += scaleField.getHeight() + PADDING;
        addButton(scaleField);

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
        scaleField.x += (panelWidth - idealWidth) / 2;

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
        else if (keyCode == GLFW.GLFW_KEY_R)
        {
            Rotation newRotation = rotation.getRotated(Rotation.CLOCKWISE_90);
            importModule.setCloneImportBoxes(selectionBox, schematic, anchor, newRotation, mirror, offset, repeat, scale);
            rotation = newRotation;
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_M)
        {
            Mirror newMirror = mirror;
            switch (mirror)
            {
                case NONE:
                    newMirror = Mirror.LEFT_RIGHT;
                    break;
                case LEFT_RIGHT:
                    newMirror = Mirror.FRONT_BACK;
                    break;
                case FRONT_BACK:
                    newMirror = Mirror.NONE;
                    break;
            }
            importModule.setCloneImportBoxes(selectionBox, schematic, anchor, rotation, newMirror, offset, repeat, scale);
            mirror = newMirror;
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
        return (NudgeButton) new NudgeButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, consumer, () -> new CloneImportBoxesHistoryEntry(selectionBox.getBoundingBox(), schematic, anchor, rotation, mirror, offset, repeat, scale, false))
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
        if (amount < 0) amount = selectionBox.getAxisSize(direction.getAxis());

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
        importModule.setCloneImportBoxes(selectionBox, schematic, anchor, rotation, mirror, offset, repeat, scale);
    }
    private void cloneButton(Button button)
    {
        importModule.placeAll(extensionsToPlace);
    }
    //endregion
}
