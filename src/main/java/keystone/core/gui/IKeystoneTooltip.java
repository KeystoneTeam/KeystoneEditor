package keystone.core.gui;

import net.minecraft.client.util.math.MatrixStack;

public interface IKeystoneTooltip
{
    void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);
}
