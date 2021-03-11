package keystone.core.gui.screens.block_selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBlockButton extends ButtonNoHotkey
{
    protected final Minecraft mc;
    protected final FontRenderer fontRenderer;
    protected final ItemStack itemStack;
    protected final BlockState block;

    protected AbstractBlockButton(ItemStack itemStack, BlockState block, int x, int y, int width, int height)
    {
        super(x, y, width, height, itemStack.getDisplayName(), button -> {});

        this.mc = Minecraft.getInstance();
        this.fontRenderer = mc.fontRenderer;
        this.itemStack = itemStack;
        this.block = block;
    }

    protected void getBlockStateTooltip(List<IFormattableTextComponent> tooltip) { tooltip.add(block.getBlock().getTranslatedName()); }
    protected abstract void onClicked(int button);

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (active && visible && isHovered())
        {
            Minecraft mc = Minecraft.getInstance();
            List<IFormattableTextComponent> text = new ArrayList<>();
            getBlockStateTooltip(text);

            fill(matrixStack, x, y, x + width, y + height, 0x80FFFFFF);
            GuiUtils.drawHoveringText(itemStack, matrixStack, text, mouseX, mouseY, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), -1, mc.fontRenderer);
        }
        KeystoneOverlay.drawItem(this, mc, itemStack, x + (width - 18) / 2 + 1, y + (height - 18) / 2 + 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.active && this.visible)
        {
            if (this.isValidClickButton(button))
            {
                boolean clicked = this.clicked(mouseX, mouseY);
                if (clicked)
                {
                    this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    this.onClick(mouseX, mouseY);
                    onClicked(button);
                    return true;
                }
            }

        }
        return false;
    }

    public BlockState getBlockState()
    {
        return block;
    }
}
