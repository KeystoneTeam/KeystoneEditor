package keystone.core.gui.overlays.block_selection;

import keystone.api.KeystoneDirectories;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.file_browser.OpenFilesScreen;
import keystone.core.gui.overlays.file_browser.SaveFileScreen;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.function.Consumer;

public class BlockPaletteEditScreen extends AbstractBlockSelectionScreen
{
    private boolean ranCallback = false;
    private final Consumer<BlockPalette> callback;
    private final BlockPalette palette;
    
    private BlockGridWidget palettePanel;

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
        this.palettePanel = BlockGridWidget.createWithViewport(this, ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.RIGHT, Viewport.MIDDLE, Viewport.RIGHT).offset(0, 0, -5, -80), true, Text.translatable("keystone.mask_panel"), (entry, mouseButton) ->
        {
            BlockType wrapper = BlockTypeRegistry.fromMinecraftBlock(entry.state());
            this.palette.without(wrapper, 1000000000);
            this.palettePanel.removeBlock(entry.state(), entry.tooltipBuilder(), 1000000000);
        }, BlockGridButton.PASS_UNMODIFIED, BlockGridButton.PASS_UNMODIFIED, BlockGridButton.CHANGE_AMOUNT);
        this.palette.forEach((blockProvider, weight) ->
        {
            for (int i = 0; i < weight; i++) palettePanel.addBlock(blockProvider.getFirst().getMinecraftBlock(), BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP, false);
        });

        this.palettePanel.rebuildButtons();
        addDrawableChild(palettePanel);
        
        // Add Save and Load Buttons
        int buttonWidth = (palettePanel.getWidth() - 5) / 2;
        addDrawableChild(new ButtonNoHotkey(palettePanel.x, palettePanel.y + palettePanel.getHeight() + 5, buttonWidth, 20, Text.translatable("keystone.save"), button ->
        {
            // Save Palette
            SaveFileScreen.saveFile("nbt", KeystoneDirectories.getPalettesDirectory(), true, palette::write);
        }));
        addDrawableChild(new ButtonNoHotkey(palettePanel.x + palettePanel.getWidth() - buttonWidth, palettePanel.y + palettePanel.getHeight() + 5, buttonWidth, 20, Text.translatable("keystone.load"), button ->
        {
            // Load Palette
            OpenFilesScreen.openFile(Text.translatable("keystone.load.palette"), Set.of("nbt"), KeystoneDirectories.getPalettesDirectory(), true, file ->
            {
                palette.read(file);
                init(client, width, height);
            }, () -> {});
        }));
        
        
        // Done and Cancel Buttons
        addDrawableChild(new ButtonNoHotkey(palettePanel.x, palettePanel.y + palettePanel.getHeight() + 30, palettePanel.getWidth(), 20, Text.translatable("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(palette);
                ranCallback = true;
            }
            close();
        }));

        addDrawableChild(new ButtonNoHotkey(palettePanel.x, palettePanel.y + palettePanel.getHeight() + 55, palettePanel.getWidth(), 20, Text.translatable("keystone.cancel"), button -> close()));
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
