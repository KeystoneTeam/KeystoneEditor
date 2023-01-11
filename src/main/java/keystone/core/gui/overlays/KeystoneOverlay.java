package keystone.core.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.modules.hotkeys.HotkeySet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class KeystoneOverlay extends Screen
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
    public void tick()
    {
        for (Element widget : children()) if (widget instanceof TextFieldWidget) ((TextFieldWidget) widget).tick();
    }
    //endregion
    //region Helper Functions
    public void checkMouseOverGui()
    {
        this.children().forEach(child ->
        {
            if (child instanceof ClickableWidget widget)
            {
                if (widget.isHovered() && widget.visible && widget.active) KeystoneGlobalState.MouseOverGUI = true;
            }
        });
    }
    public static void fillRounded(MatrixStack stack, int minX, int minY, int maxX, int maxY)
    {
        int cornerSize = 8;
        RenderSystem.setShaderTexture(0, ROUNDED_BOX);
        RenderSystem.enableBlend();

        // Corners
        drawTexture(stack, minX, minY, cornerSize, cornerSize, 0, 0, cornerSize, cornerSize, 16, 16);
        drawTexture(stack, maxX - cornerSize, minY, cornerSize, cornerSize, cornerSize, 0, cornerSize, cornerSize, 16, 16);
        drawTexture(stack, minX, maxY - cornerSize, cornerSize, cornerSize, 0, cornerSize, cornerSize, cornerSize, 16, 16);
        drawTexture(stack, maxX - cornerSize, maxY - cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, 16, 16);

        // Edges
        fill(stack, minX + cornerSize, minY, maxX - cornerSize, minY + cornerSize, 0x80000000); // TOP
        fill(stack, minX + cornerSize, maxY - cornerSize, maxX - cornerSize, maxY, 0x80000000); // BOTTOM
        fill(stack, minX, minY + cornerSize, minX + cornerSize, maxY - cornerSize, 0x80000000); // LEFT
        fill(stack, maxX - cornerSize, minY + cornerSize, maxX, maxY - cornerSize, 0x80000000); // RIGHT

        // Center
        fill(stack, minX + cornerSize, minY + cornerSize, maxX - cornerSize, maxY - cornerSize, 0x80000000);
    }

    public static void drawItem(ClickableWidget widget, MinecraftClient mc, ItemStack stack, int x, int y)
    {
        drawItem(widget, mc, stack, x, y, null);
    }
    public static void drawItem(ClickableWidget widget, MinecraftClient mc, ItemStack stack, int x, int y, String text)
    {
        mc.getItemRenderer().renderInGuiWithOverrides(stack, x, y);
        mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, stack, x, y, text);
    }
    public static void drawTooltip(IKeystoneTooltip tooltip)
    {
        KeystoneOverlayHandler.addTooltip(tooltip);
    }
    //endregion
}
