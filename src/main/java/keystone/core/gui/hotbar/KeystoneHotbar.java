package keystone.core.gui.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import keystone.core.client.Player;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.overlays.KeystonePanel;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.hotkeys.HotkeysModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class KeystoneHotbar extends KeystonePanel
{
    public static KeystoneHotbar INSTANCE = new KeystoneHotbar();

    private static KeystoneHotbarSlot selectedSlot;
    private static final Identifier hotbarTexture = new Identifier("keystone:textures/gui/hotbar.png");
    private static final float aspectRatio = 142 / 22.0f;

    private HotbarButton[] hotbarButtons;
    private float renderScale;

    private KeystoneHotbar()
    {
        super(Text.literal("keystone.screen.hotbar"));
        KeystoneHotbarEvents.CHANGED.register(this::onHotbarChanged);
    }

    //region Hotbar Changed Event
    // TODO: Separate this from the hotbar screen
    private void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
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
    protected Viewport createViewport()
    {
        Viewport dock = ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.MIDDLE);
        Viewport viewport = dock.createAspectRatioViewport(aspectRatio);
        viewport.scale(viewport.getHeight() / 22.0f);
        return viewport;
    }

    @Override
    protected void setupPanel()
    {
        hotbarButtons = new HotbarButton[]
        {
                new HotbarButton(this, KeystoneHotbarSlot.SELECTION, getSlotX(0), getViewport().getMinY() + 3),
                new HotbarButton(this, KeystoneHotbarSlot.BRUSH,     getSlotX(1), getViewport().getMinY() + 3),
                new HotbarButton(this, KeystoneHotbarSlot.CLONE,     getSlotX(2), getViewport().getMinY() + 3, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.FILL,      getSlotX(3), getViewport().getMinY() + 3, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.FILTER,    getSlotX(4), getViewport().getMinY() + 3, this::selectionBoxesPresent),
                new HotbarButton(this, KeystoneHotbarSlot.IMPORT,    getSlotX(5), getViewport().getMinY() + 3)
                {
                    @Override
                    public void onSlotClicked()
                    {
                        super.onSlotClicked();
                        Keystone.getModule(ImportModule.class).promptImportSchematic(Player.getHighlightedBlock());
                    }
                },
                new HotbarButton(this, KeystoneHotbarSlot.SPAWN,     getSlotX(6), getViewport().getMinY() + 3)
        };
        hotbarButtons[6].active = false; // Spawn
        for (HotbarButton button : hotbarButtons) addDrawableChild(button);

        if (selectedSlot == null) setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        stack.push();
        stack.scale(getViewport().getScale(), getViewport().getScale(), getViewport().getScale());

        // Draw hotbar
        RenderSystem.setShaderTexture(0, hotbarTexture);
        drawTexture(stack, getViewport().getMinX(), getViewport().getMinY(), 142, 22, 0, 0, 142, 22, 256, 256);
        super.render(stack, mouseX, mouseY, partialTicks);

        stack.pop();
    }
    public void renderToolName(MatrixStack stack, Text toolName, int mouseX, int mouseY)
    {
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
                
                if (previous != null) Keystone.getModule(HotkeysModule.class).removeHotkeySet(previous.getHotkeys());
                Keystone.getModule(HotkeysModule.class).addHotkeySet(selectedSlot.getHotkeys());
                
                KeystoneHotbarEvents.CHANGED.invoker().changed(previous, slot);
            }
        }
    }

    private int getSlotX(int slot)
    {
        return getViewport().getMinX() + (3 + slot * 20);
    }
}
