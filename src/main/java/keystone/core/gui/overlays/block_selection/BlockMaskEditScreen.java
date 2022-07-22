package keystone.core.gui.overlays.block_selection;

import keystone.api.wrappers.blocks.BlockMask;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

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
    public void onLeftClick(BlockGridButton button, int mouseButton, BlockState state)
    {
        BlockGridButton.EDIT_PROPERTIES.accept(button, mouseButton, state);
    }
    @Override
    public void onRightClick(BlockGridButton button, int mouseButton, BlockState state)
    {
        BlockGridButton.PASS_UNMODIFIED.accept(button, mouseButton, state);
    }

    @Override
    protected void init()
    {
        super.init();
        this.maskPanel = BlockGridWidget.createWithViewport(this, ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.RIGHT, Viewport.MIDDLE, Viewport.RIGHT).offset(0, 0, -5, -80), false, Text.translatable("keystone.mask_panel"), (entry, mouseButton) ->
        {
            this.mask.without(BlockTypeRegistry.fromMinecraftBlock(entry.state()));
            this.maskPanel.removeBlock(entry.state(), entry.tooltipBuilder());
        }, BlockGridButton.PASS_UNMODIFIED, BlockGridButton.PASS_UNMODIFIED);
        this.mask.forEach(
                variant -> maskPanel.addBlock(variant.getMinecraftBlock(), BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP, false),
                anyVariant -> maskPanel.addBlock(anyVariant.getDefaultState(), BlockGridWidget.ANY_VARIANT_TOOLTIP, false)
        );
        this.maskPanel.rebuildButtons();
        addDrawableChild(maskPanel);

        // Done and Cancel Buttons
        addDrawableChild(new CheckboxWidget(maskPanel.x, maskPanel.y + maskPanel.getHeight() + 5, maskPanel.getWidth(), 20, Text.translatable("keystone.blacklist"), this.mask.isBlacklist(), true)
        {
            @Override
            public void onPress()
            {
                super.onPress();
                if (isChecked()) mask.blacklist();
                else mask.whitelist();
            }
        });
        addDrawableChild(new ButtonNoHotkey(maskPanel.x, maskPanel.y + maskPanel.getHeight() + 30, maskPanel.getWidth(), 20, Text.translatable("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(mask);
                ranCallback = true;
            }
            close();
        }));
        addDrawableChild(new ButtonNoHotkey(maskPanel.x, maskPanel.y + maskPanel.getHeight() + 55, maskPanel.getWidth(), 20, Text.translatable("keystone.cancel"), button -> close()));
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
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            this.mask.with(BlockTypeRegistry.fromMinecraftBlock(entry.state()));
            this.maskPanel.addBlock(entry.state(), BlockGridWidget.NAME_AND_PROPERTIES_TOOLTIP);
        }
        else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.mask.withAllVariants(BlockTypeRegistry.fromMinecraftBlock(entry.state()));
            this.maskPanel.addBlock(entry.state(), BlockGridWidget.ANY_VARIANT_TOOLTIP);
        }
    }
}
