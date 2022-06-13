package keystone.core.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class KeystoneOverlay extends Screen
{
    private static final Identifier ROUNDED_BOX = new Identifier("keystone:textures/gui/rounded_box.png");

    private static int itemOffsetY = 0;

    private Map<ClickableWidget, Boolean> widgetsActive = new HashMap<>();
    private boolean restoreWidgets = false;

    protected KeystoneOverlay(Text titleIn)
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
    public void close()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (restoreWidgets)
        {
            for (Map.Entry<ClickableWidget, Boolean> entry : widgetsActive.entrySet()) entry.getKey().active = entry.getValue();
            restoreWidgets = false;
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
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
        widget.setZOffset(200);
        mc.getItemRenderer().zOffset = 200.0F;
        
        mc.getItemRenderer().renderInGuiWithOverrides(stack, x, y + itemOffsetY);
        mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, stack, x, y + itemOffsetY, text);

        widget.setZOffset(0);
        mc.getItemRenderer().zOffset = 0.0F;
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
    public void disableWidgets(ClickableWidget... keepActive)
    {
        this.widgetsActive.clear();
        for (Element element : children())
        {
            if (element instanceof ClickableWidget widget)
            {
                widgetsActive.put(widget, widget.active);
                widget.active = false;
            }
        }
        if (keepActive != null)
        {
            for (ClickableWidget widget : keepActive)
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