package keystone.gui.overlays.hotbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.gui.AbstractKeystoneOverlay;
import keystone.gui.screens.block_selection.SingleBlockSelectionScreen;
import keystone.modules.paste.CloneModule;
import keystone.modules.selection.SelectionModule;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

public class KeystoneHotbar extends AbstractKeystoneOverlay
{
    private static KeystoneHotbarSlot selectedSlot;
    private static final ResourceLocation hotbarTexture = new ResourceLocation("keystone:textures/gui/hotbar.png");

    private int offsetX;
    private int offsetY;
    private HotbarButton[] hotbarButtons;

    @Override
    protected void onWindowSizeChange()
    {
        offsetX = Math.round((this.mc.getMainWindow().getScaledWidth() / 2 / HotbarButton.SCALE) - 71);
        offsetY = Math.round((this.mc.getMainWindow().getScaledHeight() / HotbarButton.SCALE) - 22);

        hotbarButtons = new HotbarButton[]
        {
                new HotbarButton(this, KeystoneHotbarSlot.SELECTION, getSlotX(0), offsetY + 3),
                new HotbarButton(this, KeystoneHotbarSlot.BRUSH,     getSlotX(1), offsetY + 3),
                new HotbarButton(this, KeystoneHotbarSlot.CLONE,     getSlotX(2), offsetY + 3, () -> Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0)
                {
                    @Override
                    public void onClick()
                    {
                        Keystone.getModule(CloneModule.class).copy();
                    }
                },
                new HotbarButton(this, KeystoneHotbarSlot.FILL,      getSlotX(3), offsetY + 3, () -> Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0)
                {
                    @Override
                    public void onClick()
                    {
                        SingleBlockSelectionScreen.promptBlockStateChoice(block ->
                        {
                            Keystone.runTool(new FillTool(block));
                            selectedSlot = KeystoneHotbarSlot.SELECTION;
                        });
                    }
                },
                new HotbarButton(this, KeystoneHotbarSlot.FILTER,    getSlotX(4), offsetY + 3, () -> Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0),
                new HotbarButton(this, KeystoneHotbarSlot.IMPORT,    getSlotX(5), offsetY + 3),
                new HotbarButton(this, KeystoneHotbarSlot.SPAWN,     getSlotX(6), offsetY + 3)
        };

        if (selectedSlot == null) selectedSlot = KeystoneHotbarSlot.SELECTION;
    }
    @Override
    protected void render(MatrixStack stack)
    {
        stack.push();
        stack.scale(HotbarButton.SCALE, HotbarButton.SCALE, HotbarButton.SCALE);

        // Draw hotbar
        this.mc.getTextureManager().bindTexture(hotbarTexture);
        blit(stack, offsetX, offsetY, 142, 22, 0, 0, 142, 22, 256, 256);

        // Render slots
        for (HotbarButton button : hotbarButtons) button.render(stack);

        stack.pop();
    }

    @Override
    protected void onMouseInput(InputEvent.MouseInputEvent event)
    {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS)
        {
            for (HotbarButton button : hotbarButtons)
            {
                if (button.isHovering())
                {
                    selectedSlot = button.getSlot();
                    button.onClick();
                }
            }
        }
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
