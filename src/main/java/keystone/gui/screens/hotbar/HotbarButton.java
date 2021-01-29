package keystone.gui.screens.hotbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sun.org.apache.xpath.internal.operations.Bool;
import keystone.gui.KeystoneOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

public class HotbarButton extends Button
{
    public static final float SCALE = 1.5f;
    private static final ResourceLocation selectionTexture = new ResourceLocation("keystone:textures/gui/hotbar.png");

    private Minecraft mc;
    private final KeystoneHotbar parent;
    private final KeystoneHotbarSlot slot;
    private final Supplier<Boolean> enabledSupplier;

    private final int unscaledX;
    private final int unscaledY;

    public HotbarButton(KeystoneHotbar parent, KeystoneHotbarSlot slot, int x, int y, IPressable pressedAction)
    {
        this(parent, slot, x, y, pressedAction, () -> true);
    }
    public HotbarButton(KeystoneHotbar parent, KeystoneHotbarSlot slot, int x, int y, IPressable pressedAction, Supplier<Boolean> enabledSupplier)
    {
        super((int)(x * SCALE), (int)(y * SCALE), (int)(16 * SCALE), (int)(16 * SCALE), slot.getTitle(), pressedAction, (button, stack, mouseX, mouseY) ->
        {
            HotbarButton casted = (HotbarButton)button;
            //casted.parent.renderToolName(stack, slot.getTitle());
        });

        this.unscaledX = x;
        this.unscaledY = y;

        this.mc = parent.getMinecraft();
        this.parent = parent;
        this.slot = slot;
        this.enabledSupplier = enabledSupplier;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (enabledSupplier.get())
        {
            if (isHovered())
            {
                colorSlot(stack, 0x80FFFFFF);
                renderToolTip(stack, mouseX, mouseY);
                KeystoneOverlayHandler.MouseOverGUI = true;
            }
            if (KeystoneHotbar.getSelectedSlot() == slot)
            {
                mc.getTextureManager().bindTexture(selectionTexture);
                blit(stack, unscaledX - 4, unscaledY - 4, 24, 24, 0, 22, 24, 24, 256, 256);
            }
        }
        else colorSlot(stack, 0x80FF0000);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }

    public KeystoneHotbarSlot getSlot() { return slot; }

    public void colorSlot(MatrixStack stack, int color)
    {
        AbstractGui.fill(stack, unscaledX, unscaledY, unscaledX + 16, unscaledY + 16, color);
    }
}
