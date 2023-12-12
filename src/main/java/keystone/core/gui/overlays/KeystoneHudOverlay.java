package keystone.core.gui.overlays;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.modules.history.HistoryModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class KeystoneHudOverlay extends KeystoneOverlay
{
    public static KeystoneHudOverlay INSTANCE = new KeystoneHudOverlay();
    
    private final HistoryModule historyModule;

    private KeystoneHudOverlay()
    {
        super(Text.literal("Keystone HUD"));
        historyModule = Keystone.getModule(HistoryModule.class);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        int unsavedChanges = Math.abs(historyModule.getUnsavedChanges());
        Text unsavedChangesLabel = Text.translatable("keystone.hud.unsavedChanges", unsavedChanges);
        Text blockUpdatesLabel = Text.translatable("keystone.hud.block_updates.label").append(KeystoneGlobalState.SuppressingBlockTicks ?
                Text.translatable("keystone.hud.block_updates.off").styled(style -> style.withColor(Formatting.RED)) :
                Text.translatable("keystone.hud.block_updates.on").styled(style -> style.withColor(Formatting.GREEN)));

        // States Panel Width
        int statesWidth = client.textRenderer.getWidth(blockUpdatesLabel);
        if (unsavedChanges > 0) statesWidth = Math.max(statesWidth, client.textRenderer.getWidth(unsavedChangesLabel));

        drawLabelWithBackground(context, blockUpdatesLabel, 0, height, statesWidth);
        if (unsavedChanges != 0) drawLabelWithBackground(context, unsavedChangesLabel, 0, height - 15, statesWidth);

        super.render(context, mouseX, mouseY, partialTicks);
    }

    private void drawLabelWithBackground(DrawContext context, Text label, int x, int y, int textWidth)
    {
        context.fill(x, y, x + 8 + textWidth, y - 15, 0x80000000);
        context.fill(x + 2, y, x + 4 + textWidth, y + 1, 0xFF404040);
        context.drawTextWithShadow(textRenderer, label, x + 4, y - 11, 0xFFFFFFFF);
    }
}
