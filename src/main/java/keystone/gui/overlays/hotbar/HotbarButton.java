package keystone.gui.overlays.hotbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Supplier;

public class HotbarButton
{
    public static final float SCALE = 1.5f;
    private static final ResourceLocation selectionTexture = new ResourceLocation("keystone:textures/gui/hotbar.png");

    private Minecraft mc;
    private final KeystoneHotbar parent;
    private final KeystoneHotbarSlot slot;
    private final int x;
    private final int y;
    private final Supplier<Boolean> enabledSupplier;

    private boolean isHovering;

    public HotbarButton(KeystoneHotbar parent, KeystoneHotbarSlot slot, int x, int y) { this(parent, slot, x, y, () -> true); }
    public HotbarButton(KeystoneHotbar parent, KeystoneHotbarSlot slot, int x, int y, Supplier<Boolean> enabledSupplier)
    {
        this.mc = Minecraft.getInstance();
        this.parent = parent;
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.enabledSupplier = enabledSupplier;
    }

    public void render(MatrixStack stack)
    {
        isHovering = false;
        if (enabledSupplier.get())
        {
            if (parent.isMouseInBox((int)(this.x * SCALE), (int)(this.y * SCALE), (int)(16 * SCALE), (int)(16 * SCALE)))
            {
                isHovering = true;
                colorSlot(stack, 0x40FFFFFF);
            }

            if (parent.getSelectedSlot() == slot)
            {
                mc.getTextureManager().bindTexture(selectionTexture);
                AbstractGui.blit(stack, x - 4, y - 4, 24, 24, 0, 22, 24, 24, 256, 256);
            }
        }
        else colorSlot(stack, 0x80FF0000);
    }
    public void onClick() {  }

    public boolean isHovering() { return this.isHovering; }
    public KeystoneHotbarSlot getSlot() { return slot; }

    public void colorSlot(MatrixStack stack, int color)
    {
        AbstractGui.fill(stack, x, y, x + 16, y + 16, color);
    }
}
