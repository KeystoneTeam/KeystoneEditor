package keystone.core.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public interface IKeystoneTooltip
{
    void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float partialTicks);
    
    static IKeystoneTooltip createSimple(Text text)
    {
        return (context, textRenderer, mouseX, mouseY, partialTicks) -> context.drawTooltip(textRenderer, text, mouseX, mouseY);
    }
    static IKeystoneTooltip createSimple(List<Text> text)
    {
        return (context, textRenderer, mouseX, mouseY, partialTicks) -> context.drawTooltip(textRenderer, text, mouseX, mouseY);
    }
    static IKeystoneTooltip createSimple(Supplier<Text> textSupplier)
    {
        return (context, textRenderer, mouseX, mouseY, partialTicks) -> context.drawTooltip(textRenderer, textSupplier.get(), mouseX, mouseY);
    }
}
