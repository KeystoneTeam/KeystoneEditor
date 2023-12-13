package keystone.core.gui.overlays.block_selection;

import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.modules.filter.blocks.IBlockProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractBlockButton extends ButtonNoHotkey
{
    public interface IBlockTooltipBuilder
    {
        void buildTooltip(IBlockProvider blockProvider, int count, List<Text> tooltip);
    }

    private final IBlockTooltipBuilder tooltipBuilder;
    private final List<Text> tooltip;

    protected final MinecraftClient mc;
    protected final TextRenderer fontRenderer;
    protected final IBlockProvider blockProvider;
    protected final Screen screen;
    
    protected int count;

    protected AbstractBlockButton(Screen screen, IBlockProvider blockProvider, int x, int y, int width, int height, IBlockTooltipBuilder tooltipBuilder)
    {
        super(x, y, width, height, blockProvider.getName(), button -> {});

        this.mc = MinecraftClient.getInstance();
        this.fontRenderer = mc.textRenderer;
        this.blockProvider = blockProvider;
        this.screen = screen;

        List<Text> tooltip = new ArrayList<>();
        tooltipBuilder.buildTooltip(blockProvider, count, tooltip);
        this.tooltipBuilder = tooltipBuilder;
        this.tooltip = Collections.unmodifiableList(tooltip);
        this.count = 1;
    }

    protected abstract void onClicked(int button);

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        if (active && visible && isSelected())
        {
            context.fill(getX(), getY(), getX() + width, getY() + height, 0x80FFFFFF);
            KeystoneOverlayHandler.addTooltip(IKeystoneTooltip.createSimple(tooltip));
        }
        
        ItemStack display = blockProvider.getDisplayItem();
        display.setCount(count);
        KeystoneOverlay.drawItem(context, display, getX() + (width - 18) / 2 + 1, getY() + (height - 18) / 2 + 1);
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
                    this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                    this.onClick(mouseX, mouseY);
                    onClicked(button);
                    return true;
                }
            }

        }
        return false;
    }

    public IBlockProvider getBlockProvider() { return blockProvider; }
    public IBlockTooltipBuilder getTooltipBuilder() { return tooltipBuilder; }
}
