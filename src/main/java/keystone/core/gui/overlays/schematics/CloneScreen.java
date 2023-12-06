package keystone.core.gui.overlays.schematics;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.overlays.KeystonePanel;
import keystone.core.gui.overlays.selection.SelectionNudgeScreen;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.gui.widgets.inputs.BooleanWidget;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.CloneScreenHistoryEntry;
import keystone.core.modules.hotkeys.HotkeySet;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.extensions.ISchematicExtension;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class CloneScreen extends KeystonePanel
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
    private final HistoryModule historyModule;

    private NudgeButton nudgeImports;
    private IntegerWidget offsetX;
    private IntegerWidget offsetY;
    private IntegerWidget offsetZ;
    private IntegerWidget repeatField;
    private IntegerWidget scaleField;
    private BooleanWidget copyAir;

    protected CloneScreen(BoundingBox selectionBounds, KeystoneSchematic schematic, Vec3i anchor, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale)
    {
        super(Text.translatable("keystone.screen.clone"), true);

        importModule = Keystone.getModule(ImportModule.class);
        historyModule = Keystone.getModule(HistoryModule.class);

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
    public static void closeInstance()
    {
        if (open != null) open.close();
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }
    public static void restoreValues(BoundingBox boundingBox, KeystoneSchematic schematic, Vec3i anchor, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale, Map<Identifier, Boolean> extensionsToPlace, boolean copyAir)
    {
        if (open != null) open.close();

        open = new CloneScreen(boundingBox, schematic, anchor, rotation, mirror, offset, repeat, scale);
        KeystoneOverlayHandler.addOverlay(open);
        CloneScreen.extensionsToPlace = extensionsToPlace;
        if (open.copyAir.isChecked() != copyAir) open.copyAir.onPress();

        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.CLONE);
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
        Keystone.getModule(ImportModule.class).clearImportBoxes(true, false);
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }

    @Override
    public Viewport createViewport()
    {
        Viewport dock = ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.LEFT, Viewport.MIDDLE, Viewport.LEFT);

        int widgetsHeight = (3 * (BUTTON_HEIGHT + PADDING)) + OPTIONS_PADDING + IntegerWidget.getFinalHeight() + OPTIONS_PADDING;
        widgetsHeight += 2 * (IntegerWidget.getFinalHeight() + PADDING);
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

        // Offset Fields
        int offsetWidgetWidth = (getViewport().getWidth() - 2 * (MARGINS + PADDING)) / 3;
        offsetX = addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.offsetX"), x, y, offsetWidgetWidth, offset.x, Integer.MIN_VALUE, Integer.MAX_VALUE)
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
        offsetY = addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.offsetY"), x + offsetWidgetWidth + PADDING, y, offsetWidgetWidth, offset.y, Integer.MIN_VALUE, Integer.MAX_VALUE)
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
        offsetZ = addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.offsetZ"), x + 2 * (offsetWidgetWidth + PADDING), y, offsetWidgetWidth, offset.z, Integer.MIN_VALUE, Integer.MAX_VALUE)
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
        repeatField = (IntegerWidget) addDrawableChild(new IntegerWidget(Text.translatable("keystone.clone.repeat"), x, y, idealWidth - 2 * MARGINS, repeat, 1, Integer.MAX_VALUE)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, offset, value, scale);
                repeat = value;
                return true;
            }
        }.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.clone.repeat.tooltip"))));
        y += IntegerWidget.getFinalHeight() + PADDING;

        // Scale Field
        scaleField = (IntegerWidget) new IntegerWidget(Text.translatable("keystone.schematic_import.scale"), x, y, idealWidth - 2 * MARGINS, scale, 1, 8)
        {
            @Override
            protected boolean onSetValue(Integer value)
            {
                importModule.addCloneImportBoxes(schematic, anchor, rotation, mirror, offset, repeat, value);
                scale = value;
                return true;
            }
        }.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.schematic_import.scale.tooltip")));
        y += scaleField.getHeight() + PADDING;
        addDrawableChild(scaleField);

        // Copy Air Field
        copyAir = new BooleanWidget(x, y, getViewport().getWidth() - 2 * MARGINS, 20, Text.translatable("keystone.clone.copyAir"), true, true);
        copyAir.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.clone.copyAir.tooltip")));
        y += copyAir.getHeight() + PADDING;
        addDrawableChild(copyAir);

        for (Identifier extension : extensionsToPlace.keySet())
        {
            BooleanWidget extensionOption = createExtensionOption(y, extension);
            y += extensionOption.getHeight() + PADDING;
            addDrawableChild(extensionOption);
        }

        rotateButton.setX(rotateButton.getX() + (getViewport().getWidth() - idealWidth) / 2);
        mirrorButton.setX(mirrorButton.getX() + (getViewport().getWidth() - idealWidth) / 2);
        nudgeImports.setX(nudgeImports.getX() + (getViewport().getWidth() - idealWidth) / 2);
        repeatField.setX(repeatField.getX() + (getViewport().getWidth() - idealWidth) / 2);
        scaleField.setX(scaleField.getX() + (getViewport().getWidth() - idealWidth) / 2);

        y += OPTIONS_PADDING;
        SimpleButton cloneButton = createButton(y, "keystone.clone.clone", this::cloneButton);
        cloneButton.setTooltip(IKeystoneTooltip.createSimple(Text.translatable("keystone.clone.clone.tooltip")));
        cloneButton.setX((getViewport().getWidth() - cloneButton.getWidth()) / 2);
        addDrawableChild(cloneButton);
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
        HotkeySet hotkeySet = new HotkeySet("clone_mode");
        hotkeySet.getHotkey(GLFW.GLFW_KEY_ENTER).addListener(() -> cloneButton(null));
        hotkeySet.getHotkey(GLFW.GLFW_KEY_R).addListener(this::rotationHotkey);
        hotkeySet.getHotkey(GLFW.GLFW_KEY_M).addListener(this::mirrorHotkey);
        hotkeySet.getHotkey(GLFW.GLFW_KEY_ESCAPE).clear().addListener(this::cancelHotkey);
        return hotkeySet;
    }
    
    private void rotationHotkey()
    {
        BlockRotation newBlockRotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
        importModule.addCloneImportBoxes(schematic, anchor, newBlockRotation, mirror, offset, repeat, scale);
        rotation = newBlockRotation;
    }
    private void mirrorHotkey()
    {
        BlockMirror newBlockMirror = switch (mirror)
        {
            case NONE -> BlockMirror.LEFT_RIGHT;
            case LEFT_RIGHT -> BlockMirror.FRONT_BACK;
            case FRONT_BACK -> BlockMirror.NONE;
        };
        importModule.addCloneImportBoxes(schematic, anchor, rotation, newBlockMirror, offset, repeat, scale);
        mirror = newBlockMirror;
    }
    private void cancelHotkey()
    {
        addCloseHistoryEntry();
        close();
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
        int buttonWidth = 2 * PADDING + textRenderer.getWidth(label.getString());
        return new SimpleButton(getViewport().getMinX() + MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, IKeystoneTooltip.createSimple(Text.translatable(translationKey + ".tooltip")));
    }
    private BooleanWidget createExtensionOption(int y, Identifier extensionID)
    {
        String translationKey = extensionID.getNamespace() + "." + extensionID.getPath();
        return new BooleanWidget(getViewport().getMinX() + MARGINS, y, getViewport().getWidth() - 2 * MARGINS, 20, Text.translatable(translationKey + ".shouldPlace"), extensionsToPlace.get(extensionID), true)
        {
            @Override
            public void onPress()
            {
                super.onPress();
                extensionsToPlace.put(extensionID, isChecked());
            }
        }.setTooltip(IKeystoneTooltip.createSimple(Text.translatable(translationKey + ".tooltip")));
    }
    private void addCloseHistoryEntry()
    {
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new CloneScreenHistoryEntry(true), new CloneScreenHistoryEntry(selectionBox.getBoundingBox(), schematic, anchor, rotation, mirror, offset, repeat, scale, extensionsToPlace, copyAir.isChecked()));
        importModule.clearImportBoxes(true, true);
        historyModule.tryEndHistoryEntry();
    }
    private void addPlaceHistoryEntry()
    {
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new CloneScreenHistoryEntry(selectionBox.getBoundingBox(), schematic, anchor, rotation, mirror, offset, repeat, scale, extensionsToPlace, copyAir.isChecked()), new CloneScreenHistoryEntry(true));
        historyModule.tryEndHistoryEntry();

        historyModule.tryBeginHistoryEntry();
        importModule.placeAll(extensionsToPlace, copyAir.isChecked(), false, false);
        importModule.clearImportBoxes(true, true);
        historyModule.pushToEntry(new CloneScreenHistoryEntry(true), new CloneScreenHistoryEntry(selectionBox.getBoundingBox(), schematic, anchor, rotation, mirror, offset, repeat, scale, extensionsToPlace, copyAir.isChecked()));
        historyModule.tryEndHistoryEntry();
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
        addPlaceHistoryEntry();
    }
    //endregion
}
