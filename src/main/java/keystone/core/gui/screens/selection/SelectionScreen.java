package keystone.core.gui.screens.selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.events.KeystoneSelectionChangedEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod;

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
    public static final void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        if (event.slot == KeystoneHotbarSlot.SELECTION && Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0) open();
        else if (open != null) open.closeScreen();
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void onSelectionsChanged(final KeystoneSelectionChangedEvent event)
    {
        if (event.selections.length > 0) open();
        else if (open != null) open.closeScreen();
    }
    //endregion
    //region Screen Overrides
    @Override
    public void closeScreen()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }
    @Override
    public void onClose()
    {
        open = null;
    }

    @Override
    protected void init()
    {
        int buttonHeight = (7 * (BUTTON_HEIGHT + PADDING)) - PADDING;
        int y = (height - buttonHeight) / 2;
        panelMinY = y - MARGINS;
        panelMaxY = panelMinY + buttonHeight + MARGINS + MARGINS;
        panelWidth = 0;

        SimpleButton[] buttons = new SimpleButton[]
        {
                createButton(panelMinY + MARGINS, 0, "keystone.selection_panel.deselect", this::buttonDeselect),
                createButton(panelMinY + MARGINS, 1, "keystone.selection_panel.deleteBlocks", this::buttonDeleteBlocks),
                createButton(panelMinY + MARGINS, 2, "keystone.selection_panel.deleteEntities", this::buttonDeleteEntities),
                createButton(panelMinY + MARGINS, 3, "keystone.selection_panel.analyze", this::buttonAnalyze),
                createButton(panelMinY + MARGINS, 4, "keystone.selection_panel.cut", this::buttonCut),
                createButton(panelMinY + MARGINS, 5, "keystone.selection_panel.copy", this::buttonCopy),
                createButton(panelMinY + MARGINS, 6, "keystone.selection_panel.paste", this::buttonPaste)
        };
        buttons[2].active = false;
        buttons[3].active = false;

        for (SimpleButton button : buttons)
        {
            int width = font.getStringWidth(button.getMessage().getString());
            if (width > panelWidth) panelWidth = width;
        }
        for (SimpleButton button : buttons)
        {
            button.x = (panelWidth - font.getStringWidth(button.getMessage().getString())) / 2;
            addButton(button);
        }
        panelWidth += 2 * (PADDING + MARGINS);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, 0, panelMinY, panelWidth, panelMaxY, 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    //endregion
    //region Helpers
    private SimpleButton createButton(int startY, int index, String translationKey, Button.IPressable pressable)
    {
        TranslationTextComponent label = new TranslationTextComponent(translationKey);
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(new TranslationTextComponent(translationKey + ".tooltip"));

        int y = startY + index * (BUTTON_HEIGHT + PADDING);
        int buttonWidth = 2 * PADDING + font.getStringWidth(label.getString());
        return new SimpleButton(MARGINS, y, buttonWidth, BUTTON_HEIGHT, label, pressable, (button, stack, mouseX, mouseY) -> GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, (int)(tooltipWidth * width), font));
    }
    //endregion
    //region Button Callbacks
    private final void buttonDeselect(Button button)
    {
        selectionModule.deselect();
    }
    private final void buttonDeleteBlocks(Button button)
    {
        Keystone.runTool(new FillTool(Blocks.AIR.getDefaultState()));
    }
    private final void buttonDeleteEntities(Button button)
    {
        // TODO: Implement Delete Entities Button
    }
    private final void buttonAnalyze(Button button)
    {
        // TODO: Implement Analyze Button
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
    //endregion
}
