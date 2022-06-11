package keystone.core.gui.screens.block_selection;

import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class BlockPaletteEditScreen extends AbstractBlockSelectionScreen
{
    private boolean ranCallback = false;
    private final Consumer<BlockPalette> callback;

    private BlockGridWidget palettePanel;
    private BlockPalette palette;

    protected BlockPaletteEditScreen(BlockPalette palette, Consumer<BlockPalette> callback)
    {
        super("keystone.screen.editPalette");
        this.callback = callback;
        this.palette = palette != null ? palette : new BlockPalette();
    }
    public static void editBlockPalette(BlockPalette palette, Consumer<BlockPalette> callback)
    {
        KeystoneOverlayHandler.addOverlay(new BlockPaletteEditScreen(palette.clone(), callback));
    }

    @Override
    protected void init()
    {
        super.init();
        this.palettePanel = BlockGridWidget.createWithViewport(this, ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.RIGHT, Viewport.MIDDLE, Viewport.RIGHT).offset(0, 0, -5, -55), true, Text.translatable("keystone.mask_panel"), (entry, mouseButton) ->
        {
            BlockType wrapper = BlockTypeRegistry.fromMinecraftBlock(entry.state());
            this.palette.without(wrapper, 1);
            this.palettePanel.removeBlock(entry.state(), entry.tooltipBuilder());
        }, this::disableWidgets, this::restoreWidgets, BlockGridButton.PASS_UNMODIFIED, BlockGridButton.PASS_UNMODIFIED);
        this.palette.forEach((blockProvider, weight) ->
        {
            for (int i = 0; i < weight; i++) palettePanel.addBlock(blockProvider.getFirst().getMinecraftBlock(), BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP, false);
        });

        this.palettePanel.rebuildButtons();
        addDrawableChild(palettePanel);

        // Done and Cancel Buttons
        addDrawableChild(new ButtonNoHotkey(palettePanel.x, palettePanel.y + palettePanel.getHeight() + 5, palettePanel.getWidth(), 20, Text.translatable("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(palette);
                ranCallback = true;
            }
            close();
        }));

        addDrawableChild(new ButtonNoHotkey(palettePanel.x, palettePanel.y + palettePanel.getHeight() + 30, palettePanel.getWidth(), 20, Text.translatable("keystone.cancel"), button -> close()));
    }
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            callback.accept(palette);
            ranCallback = true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed()
    {
        if (!ranCallback)
        {
            callback.accept(null);
            ranCallback = true;
        }
        super.removed();
    }
    @Override
    public void onEntrySelected(BlockGridWidget.Entry entry, int mouseButton)
    {
        BlockType wrapper = BlockTypeRegistry.fromMinecraftBlock(entry.state());
        this.palettePanel.addBlock(entry.state(), BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP);
        this.palette.with(wrapper);
    }
}
