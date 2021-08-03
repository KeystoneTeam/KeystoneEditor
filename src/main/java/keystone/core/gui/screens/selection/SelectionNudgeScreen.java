package keystone.core.gui.screens.selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.events.KeystoneSelectionChangedEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SelectionNudgeScreen extends KeystoneOverlay
{
    private static final int MARGINS = 2;
    private static final int PADDING = 2;
    private static final int BUTTON_HEIGHT = 14;
    private static SelectionNudgeScreen open;

    private final SelectionModule selectionModule;

    private int selectionToNudge;
    private SelectionBoundingBox selectionBox;
    private String boxSize;
    private int x;
    private int y;
    private int panelWidth;
    private int panelHeight;
    private int sizeStrY;
    private int buttonWidth;

    private SimpleButton previousBoxButton;
    private SimpleButton nextBoxButton;
    private NudgeButton nudgeBox;
    private NudgeButton nudgeCorner1;
    private NudgeButton nudgeCorner2;

    protected SelectionNudgeScreen()
    {
        super(new TranslationTextComponent("keystone.screen.selectionNudge"));
        this.selectionModule = Keystone.getModule(SelectionModule.class);
        selectionToNudge = selectionModule.getSelectionBoxCount() - 1;
        selectionBox = resolveSelectionIndex();
    }
    public static void open()
    {
        if (open == null)
        {
            open = new SelectionNudgeScreen();
            if (open.selectionBox != null) KeystoneOverlayHandler.addOverlay(open);
            else open = null;
        }
    }

    //region Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        if (event.slot == KeystoneHotbarSlot.SELECTION && Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0) open();
        else if (open != null) open.onClose();
    }
    @SubscribeEvent
    public static final void onSelectionsChanged(final KeystoneSelectionChangedEvent event)
    {
        if (open == null)
        {
            if (event.selections.length > 0 && KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.SELECTION) open();
        }
        else
        {
            if (event.selections.length == 0) open.onClose();
            else if (event.createdSelection && open.selectionToNudge >= event.selections.length - 2) open.setSelectionToNudge(event.selections.length - 1);
            else if (open.selectionToNudge > event.selections.length - 1) open.setSelectionToNudge(event.selections.length - 1);
            else open.setSelectionToNudge(open.selectionToNudge);
        }
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
        updateSize();

        this.previousBoxButton = new SimpleButton(x - MARGINS - 16, y, 16, panelHeight, new StringTextComponent("<"), button -> previousBox());
        this.nextBoxButton = new SimpleButton(x + panelWidth + MARGINS, y, 16, panelHeight, new StringTextComponent(">"), button -> nextBox());
        addButton(this.previousBoxButton);
        addButton(this.nextBoxButton);

        int panelCenter = x + panelWidth / 2;
        int bottomButtonsY = y + panelHeight - MARGINS - BUTTON_HEIGHT;
        this.nudgeBox = new NudgeButton(panelCenter - buttonWidth / 2, y + MARGINS, buttonWidth, BUTTON_HEIGHT, (direction, amount) ->selectionBox.nudgeBox(direction, amount), NudgeButton.SELECTION_HISTORY_SUPPLIER);
        this.nudgeCorner1 = new NudgeButton(panelCenter - PADDING - buttonWidth, bottomButtonsY, buttonWidth, BUTTON_HEIGHT, (direction, amount) ->
        {
            selectionBox.nudgeCorner1(direction, amount);
            updateSize();
        }, NudgeButton.SELECTION_HISTORY_SUPPLIER);
        this.nudgeCorner2 = new NudgeButton(panelCenter + PADDING, bottomButtonsY, buttonWidth, BUTTON_HEIGHT, (direction, amount) ->
        {
            selectionBox.nudgeCorner2(direction, amount);
            updateSize();
        }, NudgeButton.SELECTION_HISTORY_SUPPLIER);

        this.nudgeBox.setColors(0x80C0C0C0, 0x80C0C0C0, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808080);
        this.nudgeCorner1.setColors(0x800000FF, 0x800000FF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808080);
        this.nudgeCorner2.setColors(0x80FFFF00, 0x80FFFF00, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808080);

        addButton(this.nudgeBox);
        addButton(this.nudgeCorner1);
        addButton(this.nudgeCorner2);
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        // Update visibility of previous and next box buttons
        this.previousBoxButton.visible = selectionModule.getSelectionBoxCount() > 1;
        this.nextBoxButton.visible = this.previousBoxButton.visible;

        // Background
        fill(matrixStack, x, y, x + panelWidth, y + panelHeight, 0x80000000);

        // Selected Box Size and Widgets
        drawCenteredString(matrixStack, font, boxSize, x + panelWidth / 2, sizeStrY, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick()
    {
        this.nudgeBox.tick();
        this.nudgeCorner1.tick();
        this.nudgeCorner2.tick();
        super.tick();
    }

    //endregion
    //region Helpers
    private void previousBox()
    {
        int newValue = selectionToNudge - 1;
        if (newValue < 0) newValue += selectionModule.getSelectionBoxCount();
        setSelectionToNudge(newValue);
    }
    private void nextBox()
    {
        int newValue = selectionToNudge + 1;
        if (newValue >= selectionModule.getSelectionBoxCount()) newValue -= selectionModule.getSelectionBoxCount();
        setSelectionToNudge(newValue);
    }
    private void setSelectionToNudge(int value)
    {
        selectionToNudge = value;
        selectionBox = resolveSelectionIndex();
        if (selectionBox == null) onClose();
        else updateSize();
    }
    private SelectionBoundingBox resolveSelectionIndex()
    {
        if (selectionToNudge >= selectionModule.getSelectionBoxCount()) selectionToNudge = selectionModule.getSelectionBoxCount() - 1;

        if (selectionToNudge == -1) return null;
        else return selectionModule.getSelectionBoundingBoxes().get(selectionToNudge);
    }
    private void updateSize()
    {
        boxSize = String.format("%dW x %dL x %dH", selectionBox.getSize().getX(), selectionBox.getSize().getZ(), selectionBox.getSize().getY());
        int strWidth = font.width(boxSize);
        int minWidth = 2 * MARGINS + 2 * (2 * PADDING + font.width(new TranslationTextComponent("keystone.nudge").getString())) + PADDING;
        panelWidth = Math.max(strWidth + MARGINS + MARGINS, minWidth);
        buttonWidth = panelWidth / 2 - MARGINS - PADDING / 2;
        panelHeight = 2 * MARGINS + 2 * BUTTON_HEIGHT + 2 * PADDING + 10;
        x = (width - panelWidth) / 2;
        y = KeystoneHotbar.getY() - PADDING - panelHeight;
        sizeStrY = y + panelHeight / 2 - 3;

        // Move buttons
        if (this.previousBoxButton != null)
        {
            int panelCenter = x + panelWidth / 2;
            this.previousBoxButton.x = x - MARGINS - 16;
            this.nextBoxButton.x = x + panelWidth + MARGINS;
            this.nudgeBox.x = panelCenter - buttonWidth / 2;
            this.nudgeBox.setWidth(buttonWidth);
            this.nudgeCorner1.x = panelCenter - PADDING - buttonWidth;
            this.nudgeCorner1.setWidth(buttonWidth);
            this.nudgeCorner2.x = panelCenter + PADDING;
            this.nudgeCorner2.setWidth(buttonWidth);
        }
    }
    //endregion
    //region Getters
    public static SelectionBoundingBox getSelectionToNudge()
    {
        if (open != null) return open.selectionBox;
        else return null;
    }
    //endregion
}
