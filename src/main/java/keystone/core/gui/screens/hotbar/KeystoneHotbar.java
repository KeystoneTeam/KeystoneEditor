package keystone.core.gui.screens.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import keystone.core.client.Player;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class KeystoneHotbar extends KeystoneOverlay
{
    private static KeystoneHotbarSlot selectedSlot;
    private static final Identifier hotbarTexture = new Identifier("keystone:textures/gui/hotbar.png");

    private static int offsetX;
    private static int offsetY;
    private HotbarButton[] hotbarButtons;

    public KeystoneHotbar()
    {
        super(Text.literal("keystone.screen.hotbar"));
        KeystoneHotbarEvents.CHANGED.register(this::onHotbarChanged);
    }

    //region Hotbar Changed Event
    // TODO: Separate this from the hotbar screen
    private final void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        switch (slot)
        {
            case CLONE:
                Keystone.getModule(ClipboardModule.class).copy();
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
        offsetX = Math.round((client.getWindow().getScaledWidth() / 2.0f / HotbarButton.SCALE) - 71);
        offsetY = Math.round((client.getWindow().getScaledHeight() / HotbarButton.SCALE) - 22);

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
        hotbarButtons[6].active = false; // Spawn
        for (HotbarButton button : hotbarButtons) addDrawableChild(button);

        if (selectedSlot == null) setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        stack.push();
        stack.scale(HotbarButton.SCALE, HotbarButton.SCALE, HotbarButton.SCALE);

        // Draw hotbar
        RenderSystem.setShaderTexture(0, hotbarTexture);
        drawTexture(stack, offsetX, offsetY, 142, 22, 0, 0, 142, 22, 256, 256);

        // Render slots
        boolean drawCurrentToolName = true;
        for (Element element : children())
        {
            if (element instanceof ClickableWidget widget)
            {
                widget.render(stack, mouseX, mouseY, partialTicks);
                if (widget.isHovered()) drawCurrentToolName = false;
            }
        }

        stack.pop();
    }
    public void renderToolName(MatrixStack stack, Text toolName, int mouseX, int mouseY)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<Text> text = new ArrayList<>();
        text.add(toolName);
        renderTooltip(stack, text, mouseX, mouseY);
    }

    public static KeystoneHotbarSlot getSelectedSlot()
    {
        if (selectedSlot == null) return KeystoneHotbarSlot.SELECTION;
        return selectedSlot;
    }
    public static void setSelectedSlot(KeystoneHotbarSlot slot)
    {
        if (selectedSlot != slot)
        {
            if (KeystoneHotbarEvents.ALLOW_CHANGE.invoker().allowChange(selectedSlot, slot))
            {
                KeystoneHotbarSlot previous = selectedSlot;
                selectedSlot = slot;
                KeystoneHotbarEvents.CHANGED.invoker().changed(previous, slot);
            }
        }
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
