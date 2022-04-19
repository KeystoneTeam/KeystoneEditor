package keystone.core.gui.screens.block_selection;

import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
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
        this.palettePanel = BlockGridWidget.createWithMargins(this, KeystoneHotbar.getX() + KeystoneHotbar.getWidth(), 0, KeystoneHotbar.getHeight(), 50, true, new TranslatableText("keystone.mask_panel"), state ->
        {
            BlockType wrapper = BlockTypeRegistry.fromMinecraftBlock(state);
            this.palette.without(wrapper, 1);
            this.palettePanel.removeBlock(state);
        }, this::disableWidgets, this::restoreWidgets, BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP);
        this.palette.forEach((blockProvider, weight) ->
        {
            for (int i = 0; i < weight; i++) palettePanel.addBlock(blockProvider.getFirst().getMinecraftBlock(), false);
        });

        this.palettePanel.rebuildButtons();
        addDrawableChild(palettePanel);

        // Done and Cancel Buttons
        addDrawableChild(new ButtonNoHotkey(palettePanel.x, palettePanel.y + palettePanel.getHeight() + 5, palettePanel.getWidth(), 20, new TranslatableText("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(palette);
                ranCallback = true;
            }
            removed();
        }));

        // TODO: Check if this needs to be changed from removed() to close()
        addDrawableChild(new ButtonNoHotkey(palettePanel.x, height - 20, palettePanel.getWidth(), 20, new TranslatableText("keystone.cancel"), button -> close()));
    }
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(stack, mouseX, mouseY, partialTicks);
        this.palettePanel.render(stack, mouseX, mouseY, partialTicks);
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
    public void onBlockSelected(BlockState block)
    {
        BlockType wrapper = BlockTypeRegistry.fromMinecraftBlock(block);
        this.palettePanel.addBlock(block);
        this.palette.with(wrapper);
    }
}
