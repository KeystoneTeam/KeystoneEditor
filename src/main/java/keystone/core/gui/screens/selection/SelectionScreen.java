package keystone.core.gui.screens.selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.tools.AnalyzeTool;
import keystone.api.tools.DeleteEntitiesTool;
import keystone.api.tools.FillTool;
import keystone.api.wrappers.blocks.Block;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.events.KeystoneSelectionChangedEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.file_browser.SaveFileScreen;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.modules.WorldModifierModules;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        super(new TranslationTextComponent("keystone.screen.selection"));
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

    //region Static Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        if (event.slot == KeystoneHotbarSlot.SELECTION && Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0) open();
        else if (open != null) open.onClose();
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSelectionsChanged(final KeystoneSelectionChangedEvent event)
    {
        if (event.selections.length > 0 && KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.SELECTION) open();
        else if (open != null) open.onClose();
    }
    //endregion
    //region Screen Overrides
    @Override
    public void onClose()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }
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
            int width = font.width(button.getMessage().getString());
            if (width > panelWidth) panelWidth = width;
        }
        for (SimpleButton button : buttons)
        {
            button.x = (panelWidth - font.width(button.getMessage().getString())) / 2;
            addButton(button);
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
        TranslationTextComponent label = new TranslationTextComponent(translationKey);
        int y = startY + index * (BUTTON_HEIGHT + PADDING);
        int buttonWidth = 2 * PADDING + font.width(label.getString());
        return (NudgeButton) new NudgeButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, nudgeConsumer, () -> null)
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
    private SimpleButton createButton(int startY, int index, String translationKey, Button.IPressable pressable)
    {
        TranslationTextComponent label = new TranslationTextComponent(translationKey);
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(new TranslationTextComponent(translationKey + ".tooltip"));

        int y = startY + index * (BUTTON_HEIGHT + PADDING);
        int buttonWidth = 2 * PADDING + font.width(label.getString());
        return new SimpleButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, (stack, mouseX, mouseY, partialTicks) -> GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, (int)(tooltipWidth * width), font));
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
            KeystoneSchematic schematic = KeystoneSchematic.createFromSelection(selection, worldModifiers);

            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.tryBeginHistoryEntry();

            Block air = new Block(Blocks.AIR.defaultBlockState());
            for (int x = selection.getMinCoords().getX(); x <= selection.getMaxCoords().getX(); x++)
            {
                for (int y = selection.getMinCoords().getY(); y <= selection.getMaxCoords().getY(); y++)
                {
                    for (int z = selection.getMinCoords().getZ(); z <= selection.getMaxCoords().getZ(); z++)
                    {
                        worldModifiers.blocks.setBlock(x, y, z, air);
                    }
                }
            }

            BlockPos anchor = selection.getMinCoords().toBlockPos().offset(direction.getStepX() * newAmount, direction.getStepY() * newAmount, direction.getStepZ() * newAmount);
            schematic.place(worldModifiers, anchor);

            historyModule.pushToEntry(NudgeButton.SELECTION_HISTORY_SUPPLIER.get());
            selection.nudgeBox(direction, newAmount);

            historyModule.tryEndHistoryEntry();
        });
    }
    private final void buttonDeselect(Button button)
    {
        selectionModule.deselect();
    }
    private final void buttonDeleteBlocks(Button button)
    {
        Keystone.runInternalFilter(new FillTool(Blocks.AIR.defaultBlockState()));
    }
    private final void buttonDeleteEntities(Button button)
    {
        Keystone.runInternalFilter(new DeleteEntitiesTool());
    }
    private final void buttonAnalyze(Button button)
    {
        Keystone.runInternalFilter(new AnalyzeTool());
    }
    private final void buttonCut(Button button)
    {
        Keystone.getModule(ClipboardModule.class).cut();
    }
    private final void buttonCopy(Button button)
    {
        Keystone.getModule(ClipboardModule.class).copy();
    }
    private final void buttonPaste(Button button)
    {
        Keystone.getModule(ClipboardModule.class).paste();
    }
    private final void buttonExport(Button button)
    {
        SaveFileScreen.saveFile("kschem", KeystoneDirectories.getSchematicsDirectory(), true, file ->
                Keystone.runOnMainThread(() ->
                {
                    KeystoneSchematic schematic = KeystoneSchematic.createFromSelection(SelectionNudgeScreen.getSelectionToNudge(), new WorldModifierModules());
                    SchematicLoader.saveSchematic(schematic, file);
                }));
    }
    //endregion
}
