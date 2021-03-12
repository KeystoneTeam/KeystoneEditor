package keystone.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IKeystoneTooltip
{
    void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);
}
