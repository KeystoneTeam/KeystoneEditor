package keystone.core.gui.screens.block_selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.wrappers.blocks.Block;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.properties.BlockPropertiesWidgetList;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class BlockPropertiesScreen extends KeystoneOverlay
{
    private static final int PADDING = 5;

    private boolean ranCallback = false;
    private final Consumer<Block> callback;
    private final Block block;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private BlockPropertiesWidgetList propertiesList;

    protected BlockPropertiesScreen(Block block, Consumer<Block> callback)
    {
        super(new TranslationTextComponent("keystone.screen.blockProperties"));

        this.callback = callback;
        this.block = block;
    }
    public static void editBlockProperties(Block block, Consumer<Block> callback)
    {
        if (block.getMinecraftBlock().getProperties().size() > 0) KeystoneOverlayHandler.addOverlay(new BlockPropertiesScreen(block.clone(), callback));
        else callback.accept(block);
    }

    @Override
    public void removed()
    {
        if (!ranCallback)
        {
            callback.accept(block);
            ranCallback = true;
        }
    }

    @Override
    protected void init()
    {
        // Calculate panel size
        panelWidth = minecraft.getWindow().getGuiScaledWidth() / 4;
        panelX = (minecraft.getWindow().getGuiScaledWidth() - panelWidth) / 2;
        propertiesList = new BlockPropertiesWidgetList(block, PADDING, panelWidth - PADDING - PADDING, this::disableWidgets, this::restoreWidgets);
        propertiesList.bake();
        panelHeight = PADDING + propertiesList.getHeight() + PADDING + 20 + PADDING;
        panelY = (minecraft.getWindow().getGuiScaledHeight() - panelHeight) / 2;

        // Done Button
        addButton(new ButtonNoHotkey(panelX + PADDING, panelY + PADDING + propertiesList.getHeight() + PADDING, panelWidth - 2 * PADDING, 20, new TranslationTextComponent("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(block);
                ranCallback = true;
            }
            onClose();
        }));

        // Block Properties
        propertiesList.offset(panelX + PADDING, panelY + PADDING);
        propertiesList.addWidgets(this::addButton);
        propertiesList.addQueuedWidgets(this::addButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF808080);
        fill(matrixStack, panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFF404040);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
