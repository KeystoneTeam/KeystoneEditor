package keystone.gui.screens.hotbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.gui.screens.block_selection.SingleBlockSelectionScreen;
import keystone.modules.paste.CloneModule;
import keystone.modules.selection.SelectionModule;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class KeystoneHotbar extends Screen
{
    private static KeystoneHotbarSlot selectedSlot;
    private static final ResourceLocation hotbarTexture = new ResourceLocation("keystone:textures/gui/hotbar.png");

    private int offsetX;
    private int offsetY;
    private HotbarButton[] hotbarButtons;

    public KeystoneHotbar()
    {
        super(new TranslationTextComponent("keystone.hotbar.title"));
    }

    //region Button Callbacks
    private void selectionPressed(Button button)
    {
        selectedSlot = KeystoneHotbarSlot.SELECTION;
    }
    private void brushPressed(Button button)
    {
        selectedSlot = KeystoneHotbarSlot.BRUSH;
    }
    private void clonePressed(Button button)
    {
        selectedSlot = KeystoneHotbarSlot.CLONE;
        Keystone.getModule(CloneModule.class).copy();
    }
    private void fillPressed(Button button)
    {
        selectedSlot = KeystoneHotbarSlot.FILL;
        SingleBlockSelectionScreen.promptBlockStateChoice(block ->
        {
            Keystone.runTool(new FillTool(block));
            selectedSlot = KeystoneHotbarSlot.SELECTION;
        });
    }
    private void filterPressed(Button button)
    {
        selectedSlot = KeystoneHotbarSlot.FILTER;
    }
    private void importPressed(Button button)
    {
        selectedSlot = KeystoneHotbarSlot.IMPORT;
    }
    private void spawnPressed(Button button)
    {
        selectedSlot = KeystoneHotbarSlot.SPAWN;
    }

    private boolean selectionBoxesPresent()
    {
        return Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0;
    }
    //endregion

    @Override
    public void init()
    {
        offsetX = Math.round((minecraft.getMainWindow().getScaledWidth() / 2 / HotbarButton.SCALE) - 71);
        offsetY = Math.round((minecraft.getMainWindow().getScaledHeight() / HotbarButton.SCALE) - 22);

        hotbarButtons = new HotbarButton[]
        {
                new HotbarButton(this, KeystoneHotbarSlot.SELECTION, getSlotX(0), offsetY + 3, this::selectionPressed),
                new HotbarButton(this, KeystoneHotbarSlot.BRUSH,     getSlotX(1), offsetY + 3, this::brushPressed),
                new HotbarButton(this, KeystoneHotbarSlot.CLONE,     getSlotX(2), offsetY + 3, this::clonePressed, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.FILL,      getSlotX(3), offsetY + 3, this::fillPressed, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.FILTER,    getSlotX(4), offsetY + 3, this::filterPressed, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.IMPORT,    getSlotX(5), offsetY + 3, this::importPressed),
                new HotbarButton(this, KeystoneHotbarSlot.SPAWN,     getSlotX(6), offsetY + 3, this::spawnPressed)
        };
        for (HotbarButton button : hotbarButtons) addButton(button);

        if (selectedSlot == null) selectedSlot = KeystoneHotbarSlot.SELECTION;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        stack.push();
        stack.scale(HotbarButton.SCALE, HotbarButton.SCALE, HotbarButton.SCALE);

        // Draw hotbar
        this.minecraft.getTextureManager().bindTexture(hotbarTexture);
        blit(stack, offsetX, offsetY, 142, 22, 0, 0, 142, 22, 256, 256);

        // Render slots
        for (Widget button : this.buttons) button.render(stack, mouseX, mouseY, partialTicks);

        stack.pop();
    }

    public static KeystoneHotbarSlot getSelectedSlot()
    {
        if (selectedSlot == null) return KeystoneHotbarSlot.SELECTION;
        return selectedSlot;
    }
    public static void setSelectedSlot(KeystoneHotbarSlot slot)
    {
        selectedSlot = slot;
    }
    private int getSlotX(int slot)
    {
        return offsetX + (3 + slot * 20);
    }
}
