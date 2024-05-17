package keystone.core.modules.rendering.ghost_blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import keystone.core.client.Camera;
import keystone.core.client.Player;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.schematic.KeystoneSchematic;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GhostBlocksModule implements IKeystoneModule
{
    private final Set<GhostBlocksWorld> ghostWorlds = Collections.synchronizedSet(new HashSet<>());
    private WorldCacheModule worldCache;
    
    public GhostBlocksModule()
    {
        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
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

    public void renderGhostBlocks(WorldRenderContext context)
    {
        if (isEnabled())
        {
            Vec3d cameraPos = Camera.getPosition();
            MatrixStack stack = context.matrixStack();
            stack.push();
            stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
    
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-0.1f, -0.2f);
    
            SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
            ghostWorlds.forEach(ghostWorld -> ghostWorld.getRenderer().render(stack, buffer, context.tickDelta()));
            buffer.draw();
    
            RenderSystem.polygonOffset(0, 0);
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableCull();
    
            stack.pop();
        }
    }

    private void onTick(MinecraftClient client)
    {
        if (isEnabled())
        {
            ghostWorlds.forEach(ghostWorld -> ghostWorld.getRenderer().tick());
        }
    }

    public GhostBlocksWorld createWorld(BlockRotation rotation, BlockMirror mirror)
    {
        GhostBlocksWorld world = new GhostBlocksWorld(worldCache.getDimensionWorld(Player.getDimension()), rotation, mirror);
        ghostWorlds.add(world);
        return world;
    }
    public void addWorld(GhostBlocksWorld ghostBlocks)
    {
        if (!ghostWorlds.contains(ghostBlocks)) ghostWorlds.add(ghostBlocks);
    }
    public GhostBlocksWorld createWorldFromSchematic(KeystoneSchematic schematic, BlockRotation rotation, BlockMirror mirror, int scale)
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
