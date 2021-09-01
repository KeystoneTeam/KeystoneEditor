package keystone.core.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.widgets.buttons.SimpleButton;
import keystone.core.utils.ProgressBar;
import net.minecraft.client.MainWindow;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ProgressBarOverlay extends KeystoneOverlay
{
    public static final int WIDTH = 200;
    public static final int HEIGHT = 12;

    private static boolean open;
    private static final ProgressBarOverlay overlay = new ProgressBarOverlay();
    protected ProgressBarOverlay() { super(new StringTextComponent("Progress Bar")); }

    public static void open(String title)
    {
        overlay.barTitle = title;
        if (!open)
        {
            KeystoneOverlayHandler.addOverlay(overlay);
            open = true;
        }
    }
    public static void close() { if (open) overlay.onClose(); }


    private String barTitle;
    private int centerX;
    private int centerY;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private int panelMinY;
    private int panelMaxY;

    @Override
    public void removed() { open = false; }

    @Override
    protected void init()
    {
        MainWindow window = minecraft.getWindow();
        centerX = window.getGuiScaledWidth() >> 1;
        centerY = window.getGuiScaledHeight() >> 1;
        minX = centerX - (WIDTH >> 1);
        minY = centerY - (HEIGHT >> 1);
        maxX = centerX + (WIDTH >> 1);
        maxY = centerY + (HEIGHT >> 1);

        panelMinY = minY - HEIGHT - 2;
        panelMaxY = maxY + 1;
        if (ProgressBar.isCancellable())
        {
            ITextComponent cancelLabel = new TranslationTextComponent("keystone.cancel");
            int cancelButtonWidth = 4 + font.width(cancelLabel);
            addButton(new SimpleButton(centerX - (cancelButtonWidth >> 1), maxY + 1, cancelButtonWidth, 14, cancelLabel, button -> ProgressBar.cancel())
                    .setButtonColor(0xFF808080)
                    .setBorderColor(0xFF808080));
            panelMaxY += 15;
        }
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, minX - 1, panelMinY, maxX + 1, panelMaxY, 0xFF404040);
        int fillMaxX = minX + (int)(ProgressBar.getCurrentProgress() * WIDTH);
        fill(matrixStack, minX, minY, fillMaxX, maxY, 0xFFFFFF00);

        String string = ProgressBar.getIterations() > 1 ? barTitle + " (" + ProgressBar.getCompletedIterations() + "/" + ProgressBar.getIterations() + ")" : barTitle;
        drawCenteredString(matrixStack, font, string, centerX, centerY - (HEIGHT >> 1) - 11, 0xFFFFFF00);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
