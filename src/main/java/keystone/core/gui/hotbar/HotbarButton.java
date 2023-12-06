package keystone.core.gui.hotbar;

import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
        super((int)(x * parent.getViewport().getScale()), (int)(y * parent.getViewport().getScale()), (int)(16 * parent.getViewport().getScale()), (int)(16 * parent.getViewport().getScale()), slot.getTitle(), (button) -> ((HotbarButton)button).onSlotClicked(), (context, textRenderer, mouseX, mouseY, partialTicks) -> parent.renderToolName(context, slot.getTitle(), mouseX, mouseY));

        this.unscaledX = x;
        this.unscaledY = y;

        this.mc = MinecraftClient.getInstance();
        this.parent = parent;
        this.slot = slot;
        this.enabledSupplier = enabledSupplier;
        
        setTooltipDelay(0.0f);
    }

    public void onSlotClicked()
    {
        KeystoneHotbar.setSelectedSlot(slot);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        if (active)
        {
            if (enabledSupplier.get())
            {
                if (isSelected())
                {
                    colorSlot(context, 0x80FFFFFF);
                    renderTooltip(context, mouseX, mouseY);
                }
                if (KeystoneHotbar.getSelectedSlot() == slot)
                {
                    context.drawTexture(selectionTexture, unscaledX - 4, unscaledY - 4, 24, 24, 0, 22, 24, 24, 256, 256);
                }
            }
            else colorSlot(context, 0x80FF0000);
        }
    }
    @Override
    public boolean isSelected() { return hovered && enabledSupplier.get(); }

    public KeystoneHotbarSlot getSlot() { return slot; }

    public void colorSlot(DrawContext context, int color)
    {
        context.fill(unscaledX, unscaledY, unscaledX + 16, unscaledY + 16, color);
    }
}
