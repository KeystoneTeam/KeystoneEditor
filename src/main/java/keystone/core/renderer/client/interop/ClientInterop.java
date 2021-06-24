package keystone.core.renderer.client.interop;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.client.ClientRenderer;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.DimensionId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;

public class ClientInterop
{
    public static void render(MatrixStack stack, float partialTicks, ClientPlayerEntity player)
    {
        Player.update(partialTicks, player);
        ClientRenderer.render(stack, partialTicks, DimensionId.from(player.level.dimension()));
    }
    public static void renderDeferred(float partialTicks)
    {
        ClientRenderer.renderDeferred(partialTicks);
    }

    public static int getRenderDistanceChunks()
    {
        return Minecraft.getInstance().options.renderDistance;
    }
}
