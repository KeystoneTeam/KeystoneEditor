package keystone.core.gui.screens.hotbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class HotbarButton extends ButtonNoHotkey
{
    public static final float SCALE = 2f;
    private static final ResourceLocation selectionTexture = new ResourceLocation("keystone:textures/gui/hotbar.png");

    private Minecraft mc;
    private final KeystoneHotbar parent;
    private final KeystoneHotbarSlot slot;
    private final Supplier<Boolean> enabledSupplier;

    private final int unscaledX;
    private final int unscaledY;

    public HotbarButton(KeystoneHotbar parent, KeystoneHotbarSlot slot, int x, int y)
    {
        this(parent, slot, x, y, () -> true);
    }
    public HotbarButton(KeystoneHotbar parent, KeystoneHotbarSlot slot, int x, int y, Supplier<Boolean> enabledSupplier)
    {
        super((int)(x * SCALE), (int)(y * SCALE), (int)(16 * SCALE), (int)(16 * SCALE), slot.getTitle(), (button) -> ((HotbarButton)button).onSlotClicked(), (stack, mouseX, mouseY, partialTicks) -> parent.renderToolName(stack, slot.getTitle(), mouseX, mouseY));

        this.unscaledX = x;
        this.unscaledY = y;

        this.mc = parent.getMinecraft();
        this.parent = parent;
        this.slot = slot;
        this.enabledSupplier = enabledSupplier;
    }

    public void onSlotClicked()
    {
        KeystoneHotbar.setSelectedSlot(slot);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (active)
        {
            if (enabledSupplier.get())
            {
                if (isHovered())
                {
                    colorSlot(stack, 0x80FFFFFF);
                    renderToolTip(stack, mouseX, mouseY);
                }
                if (KeystoneHotbar.getSelectedSlot() == slot)
                {
                    mc.getTextureManager().bind(selectionTexture);
                    blit(stack, unscaledX - 4, unscaledY - 4, 24, 24, 0, 22, 24, 24, 256, 256);
                }
            }
            else colorSlot(stack, 0x80FF0000);
        }
        else if (isHovered()) renderToolTip(stack, mouseX, mouseY);
    }
    @Override
    public boolean isHovered() { return isHovered && enabledSupplier.get(); }

    @Override
    protected void setFocused(boolean focused)
    {
        super.setFocused(focused);
    }

    @Override
    public boolean changeFocus(boolean focus)
    {
        return super.changeFocus(focus);
    }

    public KeystoneHotbarSlot getSlot() { return slot; }

    public void colorSlot(MatrixStack stack, int color)
    {
        fill(stack, unscaledX, unscaledY, unscaledX + 16, unscaledY + 16, color);
    }
}
