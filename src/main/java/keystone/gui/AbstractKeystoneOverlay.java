package keystone.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.client.event.InputEvent;

public abstract class AbstractKeystoneOverlay extends AbstractGui
{
    private int previousWidth;
    private int previousHeight;

    protected Vector2f normalizedPosition;
    protected Vector2f normalizedSize;

    protected Minecraft mc;
    protected FontRenderer fontRenderer;
    protected ItemRenderer itemRenderer;

    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public AbstractKeystoneOverlay()
    {
        this(0, 0, 1920, 1080);
    }
    public AbstractKeystoneOverlay(float x, float y, float width, float height)
    {
        this.mc = Minecraft.getInstance();
        this.fontRenderer = this.mc.fontRenderer;
        this.itemRenderer = this.mc.getItemRenderer();

        this.normalizedPosition = new Vector2f(x / 1920.0f, y / 1080.0f);
        this.normalizedSize = new Vector2f(width / 1920.0f, height / 1080.0f);
    }

    public void doRender(MatrixStack stack)
    {
        if (previousWidth != mc.getMainWindow().getFramebufferWidth() || previousHeight != mc.getMainWindow().getFramebufferHeight())
        {
            this.x = (int)(this.normalizedPosition.x * mc.getMainWindow().getScaledWidth());
            this.y = (int)(this.normalizedPosition.y * mc.getMainWindow().getScaledHeight());
            this.width = (int)(this.normalizedSize.x * mc.getMainWindow().getScaledWidth());
            this.height = (int)(this.normalizedSize.y * mc.getMainWindow().getScaledHeight());

            this.previousWidth = mc.getMainWindow().getFramebufferWidth();
            this.previousHeight = mc.getMainWindow().getFramebufferHeight();

            onWindowSizeChange();
        }

        render(stack);
    }

    protected void onWindowSizeChange() {  }
    protected abstract void render(MatrixStack stack);
    protected void onMouseInput(final InputEvent.MouseInputEvent event) {  }
    protected void onMouseScroll(final InputEvent.MouseScrollEvent event) {  }

    public boolean isMouseInBox(int minX, int minY, int width, int height)
    {
        int mouseX = (int)(mc.mouseHelper.getMouseX() / mc.getMainWindow().getGuiScaleFactor());
        int mouseY = (int)(mc.mouseHelper.getMouseY() / mc.getMainWindow().getGuiScaleFactor());

        boolean ret = !this.mc.mouseHelper.isMouseGrabbed() &&
                mouseX >= minX && mouseX <= minX + width &&
                mouseY >= minY && mouseY <= minY + height;

        if (ret) KeystoneOverlayHandler.MouseOverGUI = true;
        return ret;
    }
    public boolean isMouseInBox(Vector2f normalizedPosition, Vector2f normalizedSize)
    {
        double mouseX = mc.mouseHelper.getMouseX() / mc.getMainWindow().getWidth();
        double mouseY = mc.mouseHelper.getMouseY() / mc.getMainWindow().getHeight();

        boolean ret = !this.mc.mouseHelper.isMouseGrabbed() &&
                mouseX >= normalizedPosition.x && mouseX <= normalizedPosition.x + normalizedSize.x &&
                mouseY >= normalizedPosition.y && mouseY <= normalizedPosition.y + normalizedSize.y;

        if (ret) KeystoneOverlayHandler.MouseOverGUI = true;
        return ret;
    }

    public void drawItem(Item item, int x, int y)
    {
        drawItem(new ItemStack(item), x, y);
    }
    public void drawItem(ItemStack stack, int x, int y)
    {
        setBlitOffset(200);
        mc.getItemRenderer().zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = this.fontRenderer;

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);

        setBlitOffset(0);
        mc.getItemRenderer().zLevel = 0.0F;
    }
}
