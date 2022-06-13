package keystone.core.gui.overlays.block_selection;

import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.properties.BlockPropertiesWidgetList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

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
        super(Text.translatable("keystone.screen.blockProperties"));

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
        panelWidth = width / 4;
        panelX = (width - panelWidth) / 2;
        int maxPanelHeight = height - 60 - 3 * PADDING;
        propertiesList = new BlockPropertiesWidgetList(block, 0, 0, panelWidth, maxPanelHeight, PADDING, this::disableWidgets, this::restoreWidgets);
        propertiesList.bake();
        panelHeight = PADDING + propertiesList.getHeight() + PADDING + 20 + PADDING;
        panelY = (height - panelHeight) / 2;

        // Block Properties
        propertiesList.offset(panelX, panelY + PADDING);
        addDrawableChild(propertiesList);

        // Done Button
        int buttonWidth = (panelWidth - 3 * PADDING) >> 1;
        addDrawableChild(new ButtonNoHotkey(panelX + PADDING, panelY + PADDING + propertiesList.getHeight() + PADDING, buttonWidth, 20, Text.translatable("keystone.done"), button -> close()));
        addDrawableChild(new ButtonNoHotkey(panelX + panelWidth - PADDING - buttonWidth, panelY + PADDING + propertiesList.getHeight() + PADDING, buttonWidth, 20, Text.translatable("keystone.cancel"), button ->
        {
            callback.accept(null);
            ranCallback = true;
            close();
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF808080);
        fill(matrixStack, panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFF404040);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            callback.accept(null);
            ranCallback = true;
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
