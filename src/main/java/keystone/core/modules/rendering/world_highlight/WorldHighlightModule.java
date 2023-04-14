package keystone.core.modules.rendering.world_highlight;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.RendererProperties;
import keystone.core.renderer.ShapeRenderers;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class WorldHighlightModule implements IKeystoneModule
{
    private static final Color4f tileEntityFill = Color4f.yellow.withAlpha(0.125f);
    private static final Color4f entityFill = Color4f.red.withAlpha(0.125f);
    private static final Color4f invisibleEntityFill = new Color4f(1.0f, 0.5f, 0.5f, 0.125f);
    private static final Color4f specialEntityFill = Color4f.cyan.withAlpha(0.125f);
    
    private static final Color4f tileEntityOutline = tileEntityFill.withAlpha(1.0f);
    private static final Color4f entityOutline = entityFill.withAlpha(1.0f);
    private static final Color4f invisibleEntityOutline = invisibleEntityFill.withAlpha(1.0f);
    private static final Color4f specialEntityOutline = specialEntityFill.withAlpha(0.125f);
    
    private ComplexOverlayRenderer renderer;
    private boolean enabled = true;

    @Override
    public void postInit()
    {
        this.renderer = ShapeRenderers.createComplexOverlay(RendererProperties.createFill(), RendererProperties.createWireframe(2.0f).ignoreDepth());
    }

    @Override
    public boolean isEnabled() { return enabled; }
    
    public void toggle() { enabled = !enabled; }

    @Override
    public void renderWhenEnabled(WorldRenderContext context)
    {
        ClientWorld world = MinecraftClient.getInstance().world;
        AtomicReferenceArray<WorldChunk> chunks = world.getChunkManager().chunks.chunks;
        
        // For Each Client Chunk
        for (int i = 0; i < chunks.length(); i++)
        {
            // If Chunk Isn't Null
            WorldChunk chunk = chunks.get(i);
            if (chunk != null)
            {
                // Tile Entity Highlighting
                if (KeystoneConfig.highlightTileEntities && chunk.getBlockEntityPositions() != null)
                {
                    for (BlockPos pos : chunk.getBlockEntityPositions())
                    {
                        RenderBox box = new RenderBox(pos);
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box, tileEntityFill);
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box, tileEntityOutline);
                    }
                }
            }
        }
        
        // Entity Highlighting
        if (KeystoneConfig.highlightEntities)
        {
            ServerWorld serverWorld = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimension());
            for (Entity entity : serverWorld.iterateEntities())
            {
                if (entity != null && !entity.getUuid().equals(MinecraftClient.getInstance().cameraEntity.getUuid()))
                {
                    Box box = entity.getBoundingBox();
                    if (box.getXLength() == 0 && box.getYLength() == 0 && box.getZLength() == 0)
                    {
                        box = box.expand(-0.125, -0.125, -0.125);
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box, specialEntityFill);
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box, specialEntityOutline);
                    }
                    else
                    {
                        boolean invisible = entity.isInvisible();
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box, invisible ? invisibleEntityFill : entityFill);
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box, invisible ? invisibleEntityOutline : entityOutline);
                    }
                }
            }
        }
    }
}
