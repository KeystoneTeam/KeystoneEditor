package keystone.core.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.HashMap;
import java.util.Map;

public class KeystoneOverlay extends Screen
{
    private static final ResourceLocation ROUNDED_BOX = new ResourceLocation("keystone:textures/gui/rounded_box.png");

    private static int itemOffsetY = 0;

    private Map<Widget, Boolean> widgetsActive = new HashMap<>();
    private boolean restoreWidgets = false;

    protected KeystoneOverlay(ITextComponent titleIn)
    {
        super(titleIn);
    }

    //region Screen Overrides
    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }
    @Override
    public void onClose()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (restoreWidgets)
        {
            for (Map.Entry<Widget, Boolean> entry : widgetsActive.entrySet()) entry.getKey().active = entry.getValue();
            restoreWidgets = false;
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    @Override
    public void tick()
    {
        for (Widget widget : buttons) if (widget instanceof TextFieldWidget) ((TextFieldWidget) widget).tick();
    }
    //endregion
    //region Helper Functions
    public void checkMouseOverGui()
    {
        this.buttons.forEach(widget -> { if (widget.isHovered() && widget.visible && widget.active) KeystoneGlobalState.MouseOverGUI = true; });
    }
    public static void fillRounded(MatrixStack stack, int minX, int minY, int maxX, int maxY)
    {
        int cornerSize = 8;
        Minecraft.getInstance().textureManager.bind(ROUNDED_BOX);
        RenderSystem.enableBlend();

        // Corners
        blit(stack, minX, minY, cornerSize, cornerSize, 0, 0, cornerSize, cornerSize, 16, 16);
        blit(stack, maxX - cornerSize, minY, cornerSize, cornerSize, cornerSize, 0, cornerSize, cornerSize, 16, 16);
        blit(stack, minX, maxY - cornerSize, cornerSize, cornerSize, 0, cornerSize, cornerSize, cornerSize, 16, 16);
        blit(stack, maxX - cornerSize, maxY - cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, 16, 16);

        // Edges
        fill(stack, minX + cornerSize, minY, maxX - cornerSize, minY + cornerSize, 0x80000000); // TOP
        fill(stack, minX + cornerSize, maxY - cornerSize, maxX - cornerSize, maxY, 0x80000000); // BOTTOM
        fill(stack, minX, minY + cornerSize, minX + cornerSize, maxY - cornerSize, 0x80000000); // LEFT
        fill(stack, maxX - cornerSize, minY + cornerSize, maxX, maxY - cornerSize, 0x80000000); // RIGHT

        // Center
        fill(stack, minX + cornerSize, minY + cornerSize, maxX - cornerSize, maxY - cornerSize, 0x80000000);
    }

    public static void drawItem(Widget widget, Minecraft mc, ItemStack stack, int x, int y)
    {
        drawItem(widget, mc, stack, x, y, null);
    }
    public static void drawItem(Widget widget, Minecraft mc, ItemStack stack, int x, int y, String text)
    {
        widget.setBlitOffset(200);
        mc.getItemRenderer().blitOffset = 200.0F;

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = mc.font;
        mc.getItemRenderer().renderAndDecorateItem(stack, x, y + itemOffsetY);
        mc.getItemRenderer().renderGuiItemDecorations(font, stack, x, y + itemOffsetY, text);

        widget.setBlitOffset(0);
        mc.getItemRenderer().blitOffset = 0.0F;
    }
    public static void setItemOffsetY(int offset)
    {
        itemOffsetY = offset;
    }
    public static void drawTooltip(IKeystoneTooltip tooltip)
    {
        KeystoneOverlayHandler.addTooltip(tooltip);
    }
    //endregion
    //region Widgets
    public void disableWidgets(Widget... keepActive)
    {
        this.widgetsActive.clear();
        for (Widget widget : this.buttons)
        {
            widgetsActive.put(widget, widget.active);
            widget.active = false;
        }
        if (keepActive != null)
        {
            for (Widget widget : keepActive)
            {
                widgetsActive.put(widget, true);
                widget.active = true;
            }
        }
    }
    public void restoreWidgets()
    {
        this.restoreWidgets = true;
    }
    //endregion
}
