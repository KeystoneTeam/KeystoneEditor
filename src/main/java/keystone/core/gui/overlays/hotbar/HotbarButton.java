package keystone.core.gui.overlays.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class HotbarButton extends ButtonNoHotkey
{
    private static final Identifier selectionTexture = new Identifier("keystone:textures/gui/hotbar.png");

    private MinecraftClient mc;
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
        super((int)(x * parent.getViewport().getScale()), (int)(y * parent.getViewport().getScale()), (int)(16 * parent.getViewport().getScale()), (int)(16 * parent.getViewport().getScale()), slot.getTitle(), (button) -> ((HotbarButton)button).onSlotClicked(), (stack, mouseX, mouseY, partialTicks) -> parent.renderToolName(stack, slot.getTitle(), mouseX, mouseY));

        this.unscaledX = x;
        this.unscaledY = y;

        this.mc = MinecraftClient.getInstance();
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
                    renderTooltip(stack, mouseX, mouseY);
                }
                if (KeystoneHotbar.getSelectedSlot() == slot)
                {
                    RenderSystem.setShaderTexture(0, selectionTexture);
                    drawTexture(stack, unscaledX - 4, unscaledY - 4, 24, 24, 0, 22, 24, 24, 256, 256);
                }
            }
            else colorSlot(stack, 0x80FF0000);
        }
        else if (isHovered()) renderTooltip(stack, mouseX, mouseY);
    }
    @Override
    public boolean isHovered() { return hovered && enabledSupplier.get(); }

    public KeystoneHotbarSlot getSlot() { return slot; }

    public void colorSlot(MatrixStack stack, int color)
    {
        fill(stack, unscaledX, unscaledY, unscaledX + 16, unscaledY + 16, color);
    }
}
