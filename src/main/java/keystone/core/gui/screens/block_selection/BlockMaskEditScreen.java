package keystone.core.gui.screens.block_selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.TranslationTextComponent;
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
        Minecraft.getInstance().displayGuiScreen(new BlockMaskEditScreen(mask.clone(), callback));
    }

    @Override
    protected void init()
    {
        super.init();
        this.maskPanel = BlockGridWidget.createWithMargins(KeystoneHotbar.getX() + KeystoneHotbar.getWidth(), 0, KeystoneHotbar.getHeight(), 80, false, new TranslationTextComponent("keystone.mask_panel"), state ->
        {
            this.mask.without(new Block(state));
            this.maskPanel.removeBlock(state.getBlock());
        });
        this.mask.forEach(block -> maskPanel.addBlock(block.getMinecraftBlock().getBlock(), false));
        this.maskPanel.rebuildButtons();
        this.children.add(maskPanel);

        // Done and Cancel Buttons
        addButton(new CheckboxButton(maskPanel.x, maskPanel.y + maskPanel.getHeightRealms() + 5, maskPanel.getWidth(), 20, new TranslationTextComponent("keystone.blacklist"), this.mask.isBlacklist(), true)
        {
            @Override
            public void onPress()
            {
                super.onPress();
                if (isChecked()) mask.blacklist();
                else mask.whitelist();
            }
        });
        int gapCenter = (height - maskPanel.y - maskPanel.getHeightRealms()) / 2;
        addButton(new ButtonNoHotkey(maskPanel.x, maskPanel.y + maskPanel.getHeightRealms() + gapCenter - 10, maskPanel.getWidth(), 20, new TranslationTextComponent("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(mask);
                ranCallback = true;
            }
            closeScreen();
        }));
        addButton(new ButtonNoHotkey(maskPanel.x, height - 25, maskPanel.getWidth(), 20, new TranslationTextComponent("keystone.cancel"), button -> closeScreen()));
    }
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(stack, mouseX, mouseY, partialTicks);
        this.maskPanel.render(stack, mouseX, mouseY, partialTicks);
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
    public void onClose()
    {
        if (!ranCallback)
        {
            callback.accept(null);
            ranCallback = true;
        }
        super.onClose();
    }
    @Override
    public void onBlockSelected(BlockState block)
    {
        this.mask.with(new Block(block));
        this.maskPanel.addBlock(block.getBlock());
    }
}
