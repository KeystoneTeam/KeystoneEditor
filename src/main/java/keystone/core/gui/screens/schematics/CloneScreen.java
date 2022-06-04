package keystone.core.gui.screens.schematics;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.screens.selection.SelectionNudgeScreen;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.extensions.ISchematicExtension;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloneScreen extends KeystoneOverlay
{
    private static final int MARGINS = 2;
    private static final int PADDING = 5;
    private static final int OPTIONS_PADDING = 5;
    private static final int BUTTON_HEIGHT = 14;
    private static final double tooltipWidth = 0.2;
    
    private static CloneScreen open;
    private static Vec3i anchor;
    private static BlockRotation rotation;
    private static BlockMirror mirror;
    private static Vector3i offset = new Vector3i(0, 0, 0);
    private static int repeat = 1;
    private static int scale = 1;
    private static Map<Identifier, Boolean> extensionsToPlace;

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
    private CheckboxWidget copyAir;

    protected CloneScreen(BoundingBox selectionBounds, KeystoneSchematic schematic, Vec3i anchor, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale)
    {
        super(Text.translatable("keystone.screen.clone"));

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
        for (Identifier extension : schematic.getExtensionIDs())
        {
            ISchematicExtension extensionImplementation = schematic.getExtension(extension);
            if (!extensionsToPlace.containsKey(extension) && extensionImplementation.canPlace()) extensionsToPlace.put(extension, extensionImplementation.placeByDefault());
        }

        importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, offset, repeat, scale);
    }
    public static void open()
    {
        if (open == null)
        {
            SelectionBoundingBox selection = SelectionNudgeScreen.getSelectionToNudge();
            open = new CloneScreen(selection.getBoundingBox(),
                    KeystoneSchematic.createFromSelection(selection, new WorldModifierModules(), RetrievalMode.ORIGINAL, Blocks.STRUCTURE_VOID.getDefaultState()),
                    selection.getMin(), BlockRotation.NONE, BlockMirror.NONE, new Vector3i(0, 0, 0), 1, 1);
            KeystoneOverlayHandler.addOverlay(open);
        }
    }
    public static void reopen(BoundingBox bounds, KeystoneSchematic schematic, Vec3i anchor, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale)
    {
        if (open == null)
        {
            open = new CloneScreen(bounds, schematic, anchor, rotation, mirror, offset, repeat, scale);
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
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(CloneScreen::onHotbarChanged);
    }

    public static Vec3i getAnchor() { return anchor; }
    public static BlockRotation getRotation() { return rotation; }
    public static BlockMirror getMirror() { return mirror; }
    public static Vector3i getOffset() { return offset; }
    public static int getRepeat() { return repeat; }
    public static int getScale() { return scale; }
    public static Map<Identifier, Boolean> getExtensionsToPlace() { return extensionsToPlace; }
    //region Static Event Handlers
    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot == KeystoneHotbarSlot.CLONE) open();
        else if (open != null) open.close();
    }
    //endregion
    //region Screen Overrides
    @Override
    public void removed()
    {
        open = null;
        Keystone.getModule(ImportModule.class).clearImportBoxes(true);
    }

    @Override
    protected void init()
    {
        int widgetsHeight = (3 * (BUTTON_HEIGHT + PADDING)) + OPTIONS_PADDING + IntegerWidget.getFinalHeight() + OPTIONS_PADDING;
        widgetsHeight += 2 * (IntegerWidget.getFinalHeight() + PADDING);
        widgetsHeight += (extensionsToPlace.size() + 1) * (20 + PADDING);
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

        addDrawableChild(rotateButton);
        addDrawableChild(mirrorButton);
        addDrawableChild(nudgeImports);

        // Offset Fields
        int offsetWidgetWidth = (panelWidth - 2 * (MARGINS + PADDING)) / 3;
        offsetX = addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.offsetX"), MARGINS, y, offsetWidgetWidth, offset.x, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                Vector3i newOffset = new Vector3i(value, offset.y, offset.z);
                importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, newOffset, repeat, scale);
                offset = newOffset;
                return true;
            }
        });
        offsetY = addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.offsetY"), MARGINS + offsetWidgetWidth + PADDING, y, offsetWidgetWidth, offset.y, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                Vector3i newOffset = new Vector3i(offset.x, value, offset.z);
                importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, newOffset, repeat, scale);
                offset = newOffset;
                return true;
            }
        });
        offsetZ = addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.offsetZ"), MARGINS + 2 * (offsetWidgetWidth + PADDING), y, offsetWidgetWidth, offset.z, Integer.MIN_VALUE, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                Vector3i newOffset = new Vector3i(offset.x, offset.y, value);
                importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, newOffset, repeat, scale);
                offset = newOffset;
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Repeat Field
        repeatField = addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.repeat"), MARGINS, y, idealWidth - 2 * MARGINS, repeat, 1, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, offset, value, scale);
                repeat = value;
                return true;
            }
        });
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Scale Field
        scaleField = new IntegerWidget(Text.translatable("keystone.schematic_import.scale"), MARGINS, y, idealWidth - 2 * MARGINS, scale, 1, 8)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, offset, repeat, value);
                scale = value;
                return true;
            }
        };;
        y += scaleField.getHeight() + PADDING;
        addDrawableChild(scaleField);

        // Copy Air Field
        copyAir = new CheckboxWidget(MARGINS, y, panelWidth - 2 * MARGINS, 20, Text.translatable("keystone.clone.copyAir"), true, true);
        y += copyAir.getHeight() + PADDING;
        addDrawableChild(copyAir);

        for (Identifier extension : extensionsToPlace.keySet())
        {
            CheckboxWidget extensionOption = createExtensionOption(y, extension);
            y += extensionOption.getHeight() + PADDING;
            addDrawableChild(extensionOption);
        }

        rotateButton.x += (panelWidth - idealWidth) / 2;
        mirrorButton.x += (panelWidth - idealWidth) / 2;
        nudgeImports.x += (panelWidth - idealWidth) / 2;
        repeatField.x += (panelWidth - idealWidth) / 2;
        scaleField.x += (panelWidth - idealWidth) / 2;

        y += OPTIONS_PADDING;
        SimpleButton cloneButton = createButton(y, "keystone.clone.clone", this::cloneButton);
        cloneButton.x = (panelWidth - cloneButton.getWidth()) / 2;
        addDrawableChild(cloneButton);
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
            BlockRotation newBlockRotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
            importModule.addCloneImportBoxes(schematic, anchor, newBlockRotation, mirror, offset, repeat, scale);
            rotation = newBlockRotation;
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_M)
        {
            BlockMirror newBlockMirror = mirror;
            switch (mirror)
            {
                case NONE:
                    newBlockMirror = BlockMirror.LEFT_RIGHT;
                    break;
                case LEFT_RIGHT:
                    newBlockMirror = BlockMirror.FRONT_BACK;
                    break;
                case FRONT_BACK:
                    newBlockMirror = BlockMirror.NONE;
                    break;
            }
            importModule.addCloneImportBoxes(schematic, anchor, rotation, newBlockMirror, offset, repeat, scale);
            mirror = newBlockMirror;
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
        int buttonWidth = 2 * PADDING + textRenderer.getWidth(NudgeButton.NUDGE.getString());
        return (NudgeButton) new NudgeButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, consumer, null)
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
        return new SimpleButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, (stack, mouseX, mouseY, partialTicks) -> renderTooltip(stack, tooltip, mouseX, mouseY));
    }
    private CheckboxWidget createExtensionOption(int y, Identifier extensionID)
    {
        return new CheckboxWidget(MARGINS, y, panelWidth - 2 * MARGINS, 20, Text.translatable(extensionID.getNamespace() + "." + extensionID.getPath() + ".shouldPlace"), extensionsToPlace.get(extensionID), true)
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

        offsetX.setText(String.valueOf(offset.x));
        offsetY.setText(String.valueOf(offset.y));
        offsetZ.setText(String.valueOf(offset.z));
        importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, offset, repeat, scale);
    }
    private void cloneButton(ButtonWidget button)
    {
        importModule.placeAll(extensionsToPlace, copyAir.isChecked());
    }
    //endregion
}
