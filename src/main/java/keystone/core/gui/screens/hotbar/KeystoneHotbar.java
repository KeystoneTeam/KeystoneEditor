package keystone.core.gui.screens.hotbar;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.block_selection.SingleBlockSelectionScreen;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.renderer.client.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class KeystoneHotbar extends KeystoneOverlay
{
    private static KeystoneHotbarSlot selectedSlot;
    private static final ResourceLocation hotbarTexture = new ResourceLocation("keystone:textures/gui/hotbar.png");

    private static int offsetX;
    private static int offsetY;
    private HotbarButton[] hotbarButtons;

    public KeystoneHotbar()
    {
        super(new TranslationTextComponent("keystone.screen.hotbar"));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onHotbarChanged);
    }

    //region Hotbar Changed Event
    private final void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        switch (event.slot)
        {
            case CLONE:
                Keystone.getModule(ClipboardModule.class).copy();
                break;
            case FILL:
                SingleBlockSelectionScreen.promptBlockStateChoice(block ->
                {
                    Keystone.runInternalFilter(new FillTool(block));
                    setSelectedSlot(KeystoneHotbarSlot.SELECTION);
                });
                break;
        }
    }

    private boolean selectionBoxesPresent()
    {
        return Keystone.getModule(SelectionModule.class).getSelectionBoxCount() > 0;
    }
    //endregion

    @Override
    public void init()
    {
        offsetX = Math.round((minecraft.getWindow().getGuiScaledWidth() / 2.0f / HotbarButton.SCALE) - 71);
        offsetY = Math.round((minecraft.getWindow().getGuiScaledHeight() / HotbarButton.SCALE) - 22);

        hotbarButtons = new HotbarButton[]
        {
                new HotbarButton(this, KeystoneHotbarSlot.SELECTION, getSlotX(0), offsetY + 3),
                new HotbarButton(this, KeystoneHotbarSlot.BRUSH,     getSlotX(1), offsetY + 3),
                new HotbarButton(this, KeystoneHotbarSlot.CLONE,     getSlotX(2), offsetY + 3, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.FILL,      getSlotX(3), offsetY + 3, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.FILTER,    getSlotX(4), offsetY + 3, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.IMPORT,    getSlotX(5), offsetY + 3)
                {
                    @Override
                    public void onSlotClicked()
                    {
                        super.onSlotClicked();
                        Keystone.getModule(ImportModule.class).promptImportSchematic(Player.getHighlightedBlock());
                    }
                },
                new HotbarButton(this, KeystoneHotbarSlot.SPAWN,     getSlotX(6), offsetY + 3)
        };
        hotbarButtons[2].active = false; // Clone
        hotbarButtons[6].active = false; // Spawn
        for (HotbarButton button : hotbarButtons) addButton(button);

        if (selectedSlot == null) setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        stack.pushPose();
        stack.scale(HotbarButton.SCALE, HotbarButton.SCALE, HotbarButton.SCALE);

        // Draw hotbar
        this.minecraft.getTextureManager().bind(hotbarTexture);
        blit(stack, offsetX, offsetY, 142, 22, 0, 0, 142, 22, 256, 256);

        // Render slots
        boolean drawCurrentToolName = true;
        for (Widget button : this.buttons)
        {
            button.render(stack, mouseX, mouseY, partialTicks);
            if (button.isHovered()) drawCurrentToolName = false;
        }

        stack.popPose();
    }
    public void renderToolName(MatrixStack stack, IFormattableTextComponent toolName, int mouseX, int mouseY)
    {
        Minecraft mc = Minecraft.getInstance();
        List<IFormattableTextComponent> text = new ArrayList<>();
        text.add(toolName);
        GuiUtils.drawHoveringText(ItemStack.EMPTY, stack, text, mouseX, mouseY, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), -1, mc.font);
    }

    public static KeystoneHotbarSlot getSelectedSlot()
    {
        if (selectedSlot == null) return KeystoneHotbarSlot.SELECTION;
        return selectedSlot;
    }
    public static void setSelectedSlot(KeystoneHotbarSlot slot)
    {
        if (selectedSlot != slot) MinecraftForge.EVENT_BUS.post(new KeystoneHotbarEvent(slot));
        selectedSlot = slot;
    }
    public static int getX() { return (int)(offsetX * HotbarButton.SCALE); }
    public static int getY() { return (int)(offsetY * HotbarButton.SCALE); }
    public static int getWidth() { return (int)(142 * HotbarButton.SCALE); }
    public static int getHeight() { return (int)(22 * HotbarButton.SCALE); }
    private int getSlotX(int slot)
    {
        return offsetX + (3 + slot * 20);
    }
}
