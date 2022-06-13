package keystone.core.gui.overlays.selection;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.enums.RetrievalMode;
import keystone.api.tools.AnalyzeTool;
import keystone.api.tools.DeleteEntitiesTool;
import keystone.api.tools.FillTool;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.overlays.file_browser.SaveFileScreen;
import keystone.core.gui.overlays.hotbar.KeystoneHotbar;
import keystone.core.gui.overlays.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SelectionScreen extends KeystoneOverlay
{
    private static final int MARGINS = 2;
    private static final int PADDING = 5;
    private static final int BUTTON_HEIGHT = 14;
    private static final double tooltipWidth = 0.2;

    private static SelectionScreen open;

    private final SelectionModule selectionModule;
    private int panelMinY;
    private int panelMaxY;
    private int panelWidth;
    private NudgeButton nudgeButton;

    protected SelectionScreen()
    {
        super(Text.literal("keystone.screen.selection"));
        selectionModule = Keystone.getModule(SelectionModule.class);
    }
    public static void open()
    {
        if (open == null)
        {
            open = new SelectionScreen();
            KeystoneOverlayHandler.addOverlay(open);
        }
    }
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(SelectionScreen::onHotbarChanged);
        KeystoneLifecycleEvents.SELECTION_CHANGED.register(SelectionScreen::onSelectionsChanged);
    }
    //region Static Event Handlers
    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot == KeystoneHotbarSlot.SELECTION && Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0) open();
        else if (open != null) open.close();
    }
    public static void onSelectionsChanged(List<SelectionBoundingBox> selections, boolean createdSelection, boolean createHistoryEntry)
    {
        if (selections.size() > 0 && KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.SELECTION) open();
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
    protected void init()
    {
        int buttonHeight = (9 * (BUTTON_HEIGHT + PADDING)) - PADDING;
        int y = (height - buttonHeight) / 2;
        panelMinY = y - MARGINS;
        panelMaxY = panelMinY + buttonHeight + MARGINS + MARGINS;
        panelWidth = 0;

        nudgeButton = createNudgeButton(panelMinY + MARGINS, 0, "keystone.nudge", this::buttonNudge);
        SimpleButton[] buttons = new SimpleButton[]
        {
                nudgeButton,
                createButton(panelMinY + MARGINS, 1, "keystone.selection_panel.deselect", this::buttonDeselect),
                createButton(panelMinY + MARGINS, 2, "keystone.selection_panel.deleteBlocks", this::buttonDeleteBlocks),
                createButton(panelMinY + MARGINS, 3, "keystone.selection_panel.deleteEntities", this::buttonDeleteEntities),
                createButton(panelMinY + MARGINS, 4, "keystone.selection_panel.analyze", this::buttonAnalyze),
                createButton(panelMinY + MARGINS, 5, "keystone.selection_panel.cut", this::buttonCut),
                createButton(panelMinY + MARGINS, 6, "keystone.selection_panel.copy", this::buttonCopy),
                createButton(panelMinY + MARGINS, 7, "keystone.selection_panel.paste", this::buttonPaste),
                createButton(panelMinY + MARGINS, 8, "keystone.selection_panel.export", this::buttonExport)
        };

        for (SimpleButton button : buttons)
        {
            int width = textRenderer.getWidth(button.getMessage().getString());
            if (width > panelWidth) panelWidth = width;
        }
        for (SimpleButton button : buttons)
        {
            button.x = (panelWidth - textRenderer.getWidth(button.getMessage().getString())) / 2;
            addDrawableChild(button);
        }
        panelWidth += 2 * (PADDING + MARGINS);
    }

    @Override
    public void tick()
    {
        nudgeButton.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, 0, panelMinY, panelWidth, panelMaxY, 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    //endregion
    //region Helpers
    private NudgeButton createNudgeButton(int startY, int index, String translationKey, NudgeButton.NudgeConsumer nudgeConsumer)
    {
        Text label = Text.translatable(translationKey);
        int y = startY + index * (BUTTON_HEIGHT + PADDING);
        int buttonWidth = 2 * PADDING + textRenderer.getWidth(label.getString());
        return (NudgeButton) new NudgeButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, nudgeConsumer, null)
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
    private SimpleButton createButton(int startY, int index, String translationKey, ButtonWidget.PressAction pressable)
    {
        Text label = Text.translatable(translationKey);
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.translatable(translationKey + ".tooltip"));

        int y = startY + index * (BUTTON_HEIGHT + PADDING);
        int buttonWidth = 2 * PADDING + textRenderer.getWidth(label.getString());
        return new SimpleButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, (stack, mouseX, mouseY, partialTicks) -> renderTooltip(stack, tooltip, mouseX, mouseY));
    }
    //endregion
    //region Button Callbacks
    private final void buttonNudge(Direction direction, int amount)
    {
        Keystone.runOnMainThread(() ->
        {
            SelectionBoundingBox selection = SelectionNudgeScreen.getSelectionToNudge();
            int newAmount = (amount < 0) ? selection.getAxisSize(direction.getAxis()) : amount;

            WorldModifierModules worldModifiers = new WorldModifierModules();
            KeystoneSchematic schematic = KeystoneSchematic.createFromSelection(selection, worldModifiers, RetrievalMode.ORIGINAL, Blocks.STRUCTURE_VOID.getDefaultState());

            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.tryBeginHistoryEntry();

            for (int x = selection.getMin().getX(); x <= selection.getMax().getX(); x++)
            {
                for (int y = selection.getMin().getY(); y <= selection.getMax().getY(); y++)
                {
                    for (int z = selection.getMin().getZ(); z <= selection.getMax().getZ(); z++)
                    {
                        worldModifiers.blocks.setBlock(x, y, z, BlockTypeRegistry.AIR);
                    }
                }
            }

            BlockPos anchor = new BlockPos(selection.getMin().add(direction.getOffsetX() * newAmount, direction.getOffsetY() * newAmount, direction.getOffsetZ() * newAmount));
            schematic.place(worldModifiers, anchor);

            NudgeButton.SELECTION_HISTORY_CALLBACK.run();
            selection.nudgeBox(direction, newAmount);

            historyModule.tryEndHistoryEntry();
        });
    }
    private final void buttonDeselect(ButtonWidget button)
    {
        selectionModule.deselect();
    }
    private final void buttonDeleteBlocks(ButtonWidget button)
    {
        Keystone.runInternalFilters(new FillTool(Blocks.AIR.getDefaultState()));
    }
    private final void buttonDeleteEntities(ButtonWidget button)
    {
        Keystone.runInternalFilters(new DeleteEntitiesTool());
    }
    private final void buttonAnalyze(ButtonWidget button)
    {
        Keystone.runInternalFilters(new AnalyzeTool());
    }
    private final void buttonCut(ButtonWidget button)
    {
        Keystone.getModule(ClipboardModule.class).cut();
    }
    private final void buttonCopy(ButtonWidget button)
    {
        Keystone.getModule(ClipboardModule.class).copy();
    }
    private final void buttonPaste(ButtonWidget button)
    {
        Keystone.getModule(ClipboardModule.class).paste();
    }
    private final void buttonExport(ButtonWidget button)
    {
        SaveFileScreen.saveFile("kschem", KeystoneDirectories.getSchematicsDirectory(), true, file ->
                Keystone.runOnMainThread(() ->
                {
                    KeystoneSchematic schematic = KeystoneSchematic.createFromSelection(SelectionNudgeScreen.getSelectionToNudge(), new WorldModifierModules(), RetrievalMode.ORIGINAL, Blocks.STRUCTURE_VOID.getDefaultState());
                    SchematicLoader.saveSchematic(schematic, file);
                }));
    }
    //endregion
}
