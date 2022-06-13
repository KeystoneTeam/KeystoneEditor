package keystone.core.gui.overlays;

import keystone.api.Keystone;
import keystone.core.modules.history.HistoryModule;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class KeystoneHudOverlay extends KeystoneOverlay
{
    private final HistoryModule historyModule;

    public KeystoneHudOverlay()
    {
        super(Text.literal("Keystone HUD"));

        historyModule = Keystone.getModule(HistoryModule.class);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        // Unsaved Changes
        int unsavedChanges = Math.abs(historyModule.getUnsavedChanges());
        if (unsavedChanges != 0)
        {
            Text message = Text.translatable("keystone.session.unsavedChanges", unsavedChanges);
            int messageWidth = client.textRenderer.getWidth(message);
            fill(matrixStack, 0, height, 8 + messageWidth, height - 14, 0x80000000);
            client.textRenderer.drawWithShadow(matrixStack, message, 4, height - 11, 0xFFFFFFFF);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
