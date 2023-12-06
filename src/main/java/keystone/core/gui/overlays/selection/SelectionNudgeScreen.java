package keystone.core.gui.overlays.selection;

import keystone.api.Keystone;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.widgets.buttons.NudgeButton;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class SelectionNudgeScreen extends KeystoneOverlay
{
    private static final int MARGINS = 2;
    private static final int PADDING = 2;
    private static final int BUTTON_HEIGHT = 14;
    private static SelectionNudgeScreen open;
    private static SelectionBoundingBox selectedBox;
    private static int selectionToNudge;

    private final SelectionModule selectionModule;

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
        super(Text.translatable("keystone.screen.selectionNudge"));
        this.selectionModule = Keystone.getModule(SelectionModule.class);
        selectionToNudge = selectionModule.getSelectionBoxCount() - 1;
        selectedBox = resolveSelectionIndex();
    }
    public static void open()
    {
        if (open == null)
        {
            open = new SelectionNudgeScreen();
            if (selectedBox != null) KeystoneOverlayHandler.addOverlay(open);
            else open = null;
        }
    }
    public static void setSelectedIndex(int value)
    {
        selectionToNudge = value;
        if (open != null)
        {
            selectedBox = open.resolveSelectionIndex();
            if (selectedBox == null) open.close();
            else open.updateSize();
        }
    }
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(SelectionNudgeScreen::onHotbarChanged);
        KeystoneLifecycleEvents.SELECTION_CHANGED.register(SelectionNudgeScreen::onSelectionsChanged);
    }

    //region Event Handlers
    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot == KeystoneHotbarSlot.SELECTION && Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0) open();
        else if (open != null) open.close();
    }
    public static void onSelectionsChanged(List<SelectionBoundingBox> selections, boolean createdSelection, boolean createHistoryEntry)
    {
        if (selections.size() == 0) selectedBox = null;

        if (open == null)
        {
            if (selections.size() > 0 && KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.SELECTION) open();
        }
        else
        {
            if (selections.size() == 0) open.close();
            else
            {
                if (createdSelection && selectionToNudge >= selections.size() - 2) setSelectedIndex(selections.size() - 1);
                open.updateSize();
            }
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

        this.previousBoxButton = new SimpleButton(x - MARGINS - 16, y, 16, panelHeight, Text.literal("<"), button -> previousBox());
        this.nextBoxButton = new SimpleButton(x + panelWidth + MARGINS, y, 16, panelHeight, Text.literal(">"), button -> nextBox());
        addDrawableChild(this.previousBoxButton);
        addDrawableChild(this.nextBoxButton);

        int panelCenter = x + panelWidth / 2;
        int bottomButtonsY = y + panelHeight - MARGINS - BUTTON_HEIGHT;
        this.nudgeBox = new NudgeButton(panelCenter - buttonWidth / 2, y + MARGINS, buttonWidth, BUTTON_HEIGHT, (direction, amount) -> selectedBox.nudgeBox(direction, amount), NudgeButton.SELECTION_HISTORY_CALLBACK);
        this.nudgeCorner1 = new NudgeButton(panelCenter - PADDING - buttonWidth, bottomButtonsY, buttonWidth, BUTTON_HEIGHT, (direction, amount) ->
        {
            selectedBox.nudgeCorner1(direction, amount);
            updateSize();
        }, NudgeButton.SELECTION_HISTORY_CALLBACK);
        this.nudgeCorner2 = new NudgeButton(panelCenter + PADDING, bottomButtonsY, buttonWidth, BUTTON_HEIGHT, (direction, amount) ->
        {
            selectedBox.nudgeCorner2(direction, amount);
            updateSize();
        }, NudgeButton.SELECTION_HISTORY_CALLBACK);

        this.nudgeBox.setColors(0x80C0C0C0, 0x80C0C0C0, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808080);
        this.nudgeCorner1.setColors(0x800000FF, 0x800000FF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808080);
        this.nudgeCorner2.setColors(0x80FFFF00, 0x80FFFF00, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808080);

        addDrawableChild(this.nudgeBox);
        addDrawableChild(this.nudgeCorner1);
        addDrawableChild(this.nudgeCorner2);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        // Update visibility of previous and next box buttons
        this.previousBoxButton.visible = selectionModule.getSelectionBoxCount() > 1;
        this.nextBoxButton.visible = this.previousBoxButton.visible;

        // Background
        context.fill(x, y, x + panelWidth, y + panelHeight, 0x80000000);

        // Selected Box Size and Widgets
        context.drawCenteredTextWithShadow(textRenderer, boxSize, x + panelWidth / 2, sizeStrY, 0xFFFFFF);
        super.render(context, mouseX, mouseY, partialTicks);
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
        setSelectedIndex(newValue);
    }
    private void nextBox()
    {
        int newValue = selectionToNudge + 1;
        if (newValue >= selectionModule.getSelectionBoxCount()) newValue -= selectionModule.getSelectionBoxCount();
        setSelectedIndex(newValue);
    }
    private SelectionBoundingBox resolveSelectionIndex()
    {
        if (selectionToNudge >= selectionModule.getSelectionBoxCount()) selectionToNudge = selectionModule.getSelectionBoxCount() - 1;

        if (selectionToNudge == -1) return null;
        else return selectionModule.getSelectionBoundingBoxes().get(selectionToNudge);
    }
    private void updateSize()
    {
        boxSize = String.format("%dW x %dL x %dH", selectedBox.getSize().getX(), selectedBox.getSize().getZ(), selectedBox.getSize().getY());
        int strWidth = textRenderer.getWidth(boxSize);
        int minWidth = 2 * MARGINS + 2 * (2 * PADDING + textRenderer.getWidth(Text.literal("keystone.nudge").getString())) + PADDING;
        panelWidth = Math.max(strWidth + MARGINS + MARGINS, minWidth);
        buttonWidth = panelWidth / 2 - MARGINS - PADDING / 2;
        panelHeight = 2 * MARGINS + 2 * BUTTON_HEIGHT + 2 * PADDING + 10;
        x = (width - panelWidth) / 2;
        y = (int)(KeystoneHotbar.INSTANCE.getViewport().getMinY() * KeystoneHotbar.INSTANCE.getViewport().getScale()) - PADDING - panelHeight;
        sizeStrY = y + panelHeight / 2 - 3;

        // Move buttons
        if (this.previousBoxButton != null)
        {
            int panelCenter = x + panelWidth / 2;
            this.previousBoxButton.setX(x - MARGINS - 16);
            this.nextBoxButton.setX(x + panelWidth + MARGINS);
            this.nudgeBox.setX(panelCenter - buttonWidth / 2);
            this.nudgeBox.setWidth(buttonWidth);
            this.nudgeCorner1.setX(panelCenter - PADDING - buttonWidth);
            this.nudgeCorner1.setWidth(buttonWidth);
            this.nudgeCorner2.setX(panelCenter + PADDING);
            this.nudgeCorner2.setWidth(buttonWidth);
        }
    }
    //endregion
    //region Getters
    public static int getSelectionIndex() { return selectionToNudge; }
    public static SelectionBoundingBox getSelectionToNudge()
    {
        return selectedBox;
    }
    //endregion
}
