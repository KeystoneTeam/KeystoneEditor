package keystone.core.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.widgets.IMouseBlocker;
import keystone.core.gui.widgets.ITickableWidget;
import keystone.core.modules.hotkeys.HotkeySet;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class KeystoneOverlay extends Screen implements IMouseBlocker
{
    private static final Identifier ROUNDED_BOX = new Identifier("keystone:textures/gui/rounded_box.png");
    
    protected KeystoneOverlay(Text titleIn)
    {
        super(titleIn);
    }
    
    public HotkeySet getHotkeySet() { return null; }
    
    //region Screen Overrides
    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }
    @Override
    public void close()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
    
    }
    
    @Override
    public void tick()
    {
        for (Element widget : children())
        {
            if (widget instanceof ITickableWidget tickable) tickable.tick();
        }
    }
    //endregion
    
    @Override
    public boolean isMouseBlocked(double mouseX, double mouseY)
    {
        for (Element child : children())
        {
            if (child instanceof IMouseBlocker blocker && blocker.isMouseBlocked(mouseX, mouseY)) return true;
            else if (child instanceof ClickableWidget widget && widget.isSelected() && widget.visible && widget.active) return widget.isHovered();
        }
        return false;
    }
    
    //region Helper Functions
    public void fillRounded(DrawContext context, int minX, int minY, int maxX, int maxY)
    {
        int cornerSize = 8;
        RenderSystem.enableBlend();

        // Corners
        context.drawTexture(ROUNDED_BOX, minX, minY, cornerSize, cornerSize, 0, 0, cornerSize, cornerSize, 16, 16);
        context.drawTexture(ROUNDED_BOX, maxX - cornerSize, minY, cornerSize, cornerSize, cornerSize, 0, cornerSize, cornerSize, 16, 16);
        context.drawTexture(ROUNDED_BOX, minX, maxY - cornerSize, cornerSize, cornerSize, 0, cornerSize, cornerSize, cornerSize, 16, 16);
        context.drawTexture(ROUNDED_BOX, maxX - cornerSize, maxY - cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, 16, 16);

        // Edges
        context.fill(minX + cornerSize, minY, maxX - cornerSize, minY + cornerSize, 0x80000000); // TOP
        context.fill(minX + cornerSize, maxY - cornerSize, maxX - cornerSize, maxY, 0x80000000); // BOTTOM
        context.fill(minX, minY + cornerSize, minX + cornerSize, maxY - cornerSize, 0x80000000); // LEFT
        context.fill(maxX - cornerSize, minY + cornerSize, maxX, maxY - cornerSize, 0x80000000); // RIGHT

        // Center
        context.fill(minX + cornerSize, minY + cornerSize, maxX - cornerSize, maxY - cornerSize, 0x80000000);
    }

    public static void drawItem(DrawContext context, ItemStack stack, int x, int y)
    {
        drawItem(context, stack, x, y, null);
    }
    public static void drawItem(DrawContext context, ItemStack stack, int x, int y, String text)
    {
        context.drawItem(stack, x, y);
        //mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, stack, x, y, text);
    }
    //endregion
}
