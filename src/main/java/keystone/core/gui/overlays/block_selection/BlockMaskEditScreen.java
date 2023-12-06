package keystone.core.gui.overlays.block_selection;

import keystone.api.KeystoneDirectories;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.file_browser.OpenFilesScreen;
import keystone.core.gui.overlays.file_browser.SaveFileScreen;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.BooleanWidget;
import keystone.core.modules.filter.blocks.IBlockProvider;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.function.Consumer;

public class BlockMaskEditScreen extends AbstractBlockSelectionScreen
{
    private boolean ranCallback = false;
    private final Consumer<BlockMask> callback;

    private BlockGridWidget maskPanel;
    private BlockMask mask;

    protected BlockMaskEditScreen(BlockMask mask, Consumer<BlockMask> callback)
    {
        super("keystone.screen.editMask");
        this.callback = callback;
        this.mask = mask != null ? mask : new BlockMask();
    }
    public static void editBlockMask(BlockMask mask, Consumer<BlockMask> callback)
    {
        KeystoneOverlayHandler.addOverlay(new BlockMaskEditScreen(mask.clone(), callback));
    }

    @Override
    public void onLeftClick(BlockGridButton button, int mouseButton, IBlockProvider blockProvider)
    {
        BlockGridButton.EDIT_PROPERTIES.accept(button, mouseButton, blockProvider);
    }
    @Override
    public void onRightClick(BlockGridButton button, int mouseButton, IBlockProvider blockProvider)
    {
        BlockGridButton.PASS_UNMODIFIED.accept(button, mouseButton, blockProvider);
    }

    @Override
    protected void init()
    {
        super.init();
        this.maskPanel = BlockGridWidget.createWithViewport(this, ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.RIGHT, Viewport.MIDDLE, Viewport.RIGHT).offset(0, 0, -5, -105), false, Text.translatable("keystone.mask_panel"), (entry, mouseButton) ->
        {
            entry.provider().forEach(blockType -> this.mask.without(blockType));
            this.maskPanel.removeBlockProvider(entry.provider(), entry.tooltipBuilder());
        }, BlockGridButton.PASS_UNMODIFIED, BlockGridButton.PASS_UNMODIFIED, BlockGridButton.NO_SCROLLING);
        this.mask.forEach(
                variant -> maskPanel.addBlockProvider(variant, BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP, false),
                anyVariant -> maskPanel.addBlockProvider(anyVariant, BlockGridWidget.ANY_VARIANT_TOOLTIP, false)
        );
        this.maskPanel.rebuildButtons();
        addDrawableChild(maskPanel);
        
        // Blacklist Toggle
        addDrawableChild(new BooleanWidget(maskPanel.getX(), maskPanel.getY() + maskPanel.getHeight() + 5, maskPanel.getWidth(), 20, Text.translatable("keystone.blacklist"), this.mask.isBlacklist(), true)
        {
            @Override
            public void onPress()
            {
                super.onPress();
                if (isChecked()) mask.blacklist();
                else mask.whitelist();
            }
        }.setTooltip(IKeystoneTooltip.createSimple(() -> mask.isBlacklist() ? Text.translatable("keystone.blacklist.tooltip") : Text.translatable("keystone.whitelist.tooltip"))));
        
        // Save and Load Buttons
        int buttonWidth = (maskPanel.getWidth() - 5) / 2;
        addDrawableChild(new ButtonNoHotkey(maskPanel.getX(), maskPanel.getY() + maskPanel.getHeight() + 30, buttonWidth, 20, Text.translatable("keystone.save"), button ->
        {
            // Save Mask
            SaveFileScreen.saveFile("nbt", KeystoneDirectories.getMasksDirectory(), true, mask::write);
        }));
        addDrawableChild(new ButtonNoHotkey(maskPanel.getX() + maskPanel.getWidth() - buttonWidth, maskPanel.getY() + maskPanel.getHeight() + 30, buttonWidth, 20, Text.translatable("keystone.load"), button ->
        {
            // Load Mask
            OpenFilesScreen.openFile(Text.translatable("keystone.load.mask"), Set.of("nbt"), KeystoneDirectories.getMasksDirectory(), true, file ->
            {
                mask.read(file);
                init(client, width, height);
            }, () -> {});
        }));
        
        // Done and Cancel Buttons
        addDrawableChild(new ButtonNoHotkey(maskPanel.getX(), maskPanel.getY() + maskPanel.getHeight() + 55, maskPanel.getWidth(), 20, Text.translatable("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(mask);
                ranCallback = true;
            }
            close();
        }));
        addDrawableChild(new ButtonNoHotkey(maskPanel.getX(), maskPanel.getY() + maskPanel.getHeight() + 80, maskPanel.getWidth(), 20, Text.translatable("keystone.cancel"), button -> close()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
        {
            callback.accept(mask);
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
        // TODO: CRITICAL - Finish implementing this
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            this.mask.with(entry.provider());
            this.maskPanel.addBlockProvider(entry.provider(), BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP);
        }
        else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.mask.withAllVariants(entry.provider());
            this.maskPanel.addBlockProvider(entry.provider(), BlockGridWidget.ANY_VARIANT_TOOLTIP);
        }
    }
}
