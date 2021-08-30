package keystone.core.gui.screens.block_selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
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
    private final Consumer<BlockType> callback;
    private final Block block;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private BlockPropertiesWidgetList propertiesList;

    protected BlockPropertiesScreen(BlockType blockType, Consumer<BlockType> callback)
    {
        super(new TranslationTextComponent("keystone.screen.blockProperties"));

        this.callback = callback;
        this.block = new Block(blockType);
    }
    public static void editBlockProperties(BlockType blockType, Consumer<BlockType> callback)
    {
        if (blockType.getMinecraftBlock().getProperties().size() > 0) KeystoneOverlayHandler.addOverlay(new BlockPropertiesScreen(blockType, callback));
        else callback.accept(blockType);
    }

    @Override
    public void removed()
    {
        if (!ranCallback)
        {
            callback.accept(block.blockType());
            ranCallback = true;
        }
    }

    @Override
    protected void init()
    {
        // Calculate panel size
        panelWidth = minecraft.getWindow().getGuiScaledWidth() / 4;
        panelX = (minecraft.getWindow().getGuiScaledWidth() - panelWidth) / 2;
        int maxPanelHeight = minecraft.getWindow().getGuiScaledHeight() - 60 - 3 * PADDING;
        propertiesList = new BlockPropertiesWidgetList(block, 0, 0, panelWidth, maxPanelHeight, PADDING, this::disableWidgets, this::restoreWidgets);
        propertiesList.bake();
        panelHeight = PADDING + propertiesList.getHeight() + PADDING + 20 + PADDING;
        panelY = (minecraft.getWindow().getGuiScaledHeight() - panelHeight) / 2;

        // Done Button
        addButton(new ButtonNoHotkey(panelX + PADDING, panelY + PADDING + propertiesList.getHeight() + PADDING, panelWidth - 2 * PADDING, 20, new TranslationTextComponent("keystone.done"), button ->
        {
            if (!ranCallback)
            {
                callback.accept(block.blockType());
                ranCallback = true;
            }
            onClose();
        }));

        // Block Properties
        propertiesList.offset(panelX, panelY);
        addButton(propertiesList);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF808080);
        fill(matrixStack, panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFF404040);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
