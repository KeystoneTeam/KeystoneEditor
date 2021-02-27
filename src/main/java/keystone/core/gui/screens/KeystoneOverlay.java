package keystone.core.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.HashMap;
import java.util.Map;

public class KeystoneOverlay extends Screen
{
    private static final ResourceLocation ROUNDED_BOX = new ResourceLocation("keystone:textures/gui/rounded_box.png");

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
    public void closeScreen()
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
    //endregion
    //region Helper Functions
    public void checkMouseOverGui()
    {
        this.buttons.forEach(widget -> { if (widget.isHovered() && widget.visible && widget.active) KeystoneGlobalState.MouseOverGUI = true; });
    }
    public static void fillRounded(MatrixStack stack, int minX, int minY, int maxX, int maxY)
    {
        int cornerSize = 8;
        Minecraft.getInstance().textureManager.bindTexture(ROUNDED_BOX);
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
