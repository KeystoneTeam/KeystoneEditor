package keystone.core.modules.ghost_blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.modules.IKeystoneModule;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.client.Camera;
import keystone.core.renderer.client.renderers.RenderQueue;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class GhostBlocksModule implements IKeystoneModule
{
    private Set<GhostBlocksWorld> ghostWorlds = new HashSet<>();

    public GhostBlocksModule()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event)
    {
        ghostWorlds.forEach(ghostWorld -> ghostWorld.getRenderer().tick());
    }
    @SubscribeEvent
    public void onRenderWorld(final RenderWorldLastEvent event)
    {
        RenderQueue.render(() ->
        {
            Vector3d cameraPos = Camera.getPos();

            MatrixStack stack = event.getMatrixStack();
            stack.push();
            stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

            ghostWorlds.forEach(ghostWorld -> ghostWorld.getRenderer().render(stack, buffer, event.getPartialTicks()));

            buffer.finish();
            RenderSystem.enableCull();
            stack.pop();
        });
    }
    @SubscribeEvent
    public void onKeyPressed(final InputEvent.KeyInputEvent event)
    {
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_U)
        {
            ghostWorlds.forEach(world -> world.getRenderer().markDirty());
            Minecraft.getInstance().player.sendMessage(new StringTextComponent("Refreshing ghost blocks"), null);
        }
    }

    public GhostBlocksWorld createWorld()
    {
        GhostBlocksWorld world = new GhostBlocksWorld(Minecraft.getInstance().world);
        ghostWorlds.add(world);
        return world;
    }
    public void addWorld(GhostBlocksWorld ghostBlocks)
    {
        if (!ghostWorlds.contains(ghostBlocks)) ghostWorlds.add(ghostBlocks);
    }
    public GhostBlocksWorld createWorldFromSchematic(KeystoneSchematic schematic)
    {
        GhostBlocksWorld world = createWorld();
        schematic.place(BlockPos.ZERO, world);
        return world;
    }
    public void releaseWorld(GhostBlocksWorld world)
    {
        ghostWorlds.remove(world);
    }
}
