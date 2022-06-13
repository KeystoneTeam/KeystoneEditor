package keystone.core.gui.overlays.fill;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.KeystonePanel;
import keystone.core.gui.overlays.block_selection.SingleBlockSelectionScreen;
import keystone.core.gui.overlays.hotbar.KeystoneHotbar;
import keystone.core.gui.overlays.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.BlockMaskWidget;
import keystone.core.gui.widgets.inputs.BlockPaletteWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FillAndReplaceScreen extends KeystonePanel
{
    private static final int PADDING = 5;

    //region Static
    private static final FillAndReplaceScreen instance = new FillAndReplaceScreen();
    private static boolean open;
    private static BlockMask previousMask;
    private static BlockPalette previousPalette;
    private static SingleBlockSelectionScreen quickFill;

    protected FillAndReplaceScreen()
    {
        super(Text.translatable("keystone.screen.fill"));
        previousMask = new BlockMask().blacklist();
        previousPalette = new BlockPalette();
    }
    public static void open()
    {
        if (!open)
        {
            KeystoneOverlayHandler.addOverlay(instance);
            open = true;
        }
    }
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(FillAndReplaceScreen::onHotbarChanged);
    }
    //endregion

    private BlockMaskWidget mask;
    private BlockPaletteWidget palette;

    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot != KeystoneHotbarSlot.FILL)
        {
            if (open) instance.close();
            if (quickFill != null)
            {
                quickFill.close();
                quickFill = null;
            }
        }
        else if (!open && quickFill == null)
        {
            if (Screen.hasControlDown())
            {
                quickFill = SingleBlockSelectionScreen.promptBlockStateChoice(blockType ->
                {
                    quickFill = null;
                    if (blockType != null) Keystone.runInternalFilters(new FillTool(blockType));
                    KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
                });
            }
            else open();
        }
    }

    @Override
    public void removed()
    {
        open = false;
        previousMask = mask.getMask();
        previousPalette = palette.getPalette();
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }

    @Override
    protected Viewport createViewport()
    {
        Viewport dock = ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.LEFT, Viewport.MIDDLE, Viewport.LEFT);
        int widgetSize = BlockMaskWidget.getFinalHeight() + BlockPaletteWidget.getFinalHeight() + 20 + 2 * PADDING;
        return dock.createLeftCenteredViewport(widgetSize + 2 * PADDING);
    }
    @Override
    protected void setupPanel()
    {
        int y = getViewport().getMinY() + PADDING;
        int width = getViewport().getWidth() - 2 * PADDING;
        mask = new BlockMaskWidget(Text.translatable("keystone.fill.mask"), PADDING, y, width, previousMask, this::disableWidgets, this::restoreWidgets);
        y += mask.getHeight() + PADDING;
        palette = new BlockPaletteWidget(Text.translatable("keystone.fill.palette"), PADDING, y, width, previousPalette, this::disableWidgets, this::restoreWidgets);
        y += palette.getHeight() + PADDING;

        int buttonWidth = (getViewport().getWidth() - 3 * PADDING) >> 1;
        ButtonNoHotkey fillButton = new ButtonNoHotkey(PADDING, y, buttonWidth, 20, Text.translatable("keystone.fill.fill"), this::fillButton);
        ButtonNoHotkey cancelButton = new ButtonNoHotkey(getViewport().getMaxX() - PADDING - buttonWidth, y, buttonWidth, 20, Text.translatable("keystone.cancel"), button -> close());

        addDrawableChild(mask);
        addDrawableChild(palette);
        addDrawableChild(fillButton);
        addDrawableChild(cancelButton);
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fillPanel(matrixStack, 0x80000000);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            close();
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            fillButton(null);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void fillButton(ButtonWidget button)
    {
        FillTool fillTool = new FillTool(mask.getMask(), palette.getPalette());
        Keystone.runInternalFilters(fillTool);
        close();
    }
}