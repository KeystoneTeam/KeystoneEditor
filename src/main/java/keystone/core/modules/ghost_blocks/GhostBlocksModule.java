package keystone.core.modules.ghost_blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.client.Camera;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.renderers.RenderQueue;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GhostBlocksModule implements IKeystoneModule
{
    private WorldCacheModule worldCache;
    private Set<GhostBlocksWorld> ghostWorlds = Collections.synchronizedSet(new HashSet<>());

    public GhostBlocksModule()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
    @Override
    public void postInit()
    {
        this.worldCache = Keystone.getModule(WorldCacheModule.class);
    }
    @Override
    public void resetModule()
    {
        ghostWorlds.clear();
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
            Vector3d cameraPos = Camera.getPosition();
            MatrixStack stack = event.getMatrixStack();
            stack.pushPose();
            stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

            ghostWorlds.forEach(ghostWorld -> ghostWorld.getRenderer().render(stack, buffer, event.getPartialTicks()));

            buffer.endBatch();
            RenderSystem.enableCull();
            stack.popPose();
        });
    }

    public GhostBlocksWorld createWorld(Rotation rotation, Mirror mirror)
    {
        GhostBlocksWorld world = new GhostBlocksWorld(worldCache.getDimensionWorld(Player.getDimensionId()), rotation, mirror);
        ghostWorlds.add(world);
        return world;
    }
    public void addWorld(GhostBlocksWorld ghostBlocks)
    {
        if (!ghostWorlds.contains(ghostBlocks)) ghostWorlds.add(ghostBlocks);
    }
    public GhostBlocksWorld createWorldFromSchematic(KeystoneSchematic schematic, Rotation rotation, Mirror mirror, int scale)
    {
        GhostBlocksWorld world = createWorld(rotation, mirror);
        schematic.place(world, scale);
        return world;
    }
    public void updateWorldFromSchematic(GhostBlocksWorld world, KeystoneSchematic schematic, int scale)
    {
        world.clearAllContents();
        schematic.place(world, scale);
    }
    public void releaseWorld(GhostBlocksWorld world)
    {
        ghostWorlds.remove(world);
    }
}
