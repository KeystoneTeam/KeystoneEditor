package keystone.core.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public interface IKeystoneTooltip
{
    void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks);
    
    static IKeystoneTooltip createSimple(Screen screen, Text text)
    {
        return (matrices, mouseX, mouseY, partialTicks) -> screen.renderTooltip(matrices, text, mouseX, mouseY);
    }
    static IKeystoneTooltip createSimple(Screen screen, Supplier<Text> textSupplier)
    {
        return (matrices, mouseX, mouseY, partialTicks) -> screen.renderTooltip(matrices, textSupplier.get(), mouseX, mouseY);
    }
}
