package keystone.core.modules.rendering.world_highlight;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.mixins.ThreadedAnvilChunkStorageInvoker;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.ShapeRenderers;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.RendererProperties;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;

public class WorldHighlightModule implements IKeystoneModule
{
    private static final Color4f tileEntityFill = Color4f.yellow.withAlpha(0.125f);
    private static final Color4f tileEntityOutline = tileEntityFill.withAlpha(1.0f);

    private WorldCacheModule worldCache;
    private ComplexOverlayRenderer renderer;

    @Override
    public void postInit()
    {
        this.worldCache = Keystone.getModule(WorldCacheModule.class);
        this.renderer = ShapeRenderers.createComplexOverlay(RendererProperties.createFill().ignoreDepth(), RendererProperties.createWireframe(2.0f).ignoreDepth());
    }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void renderWhenEnabled(WorldRenderContext context)
    {
        if (KeystoneGlobalState.HighlightTileEntities)
        {
            ThreadedAnvilChunkStorage storage = worldCache.getDimensionWorld(Player.getDimension()).getChunkManager().threadedAnvilChunkStorage;
            ThreadedAnvilChunkStorageInvoker invoker = (ThreadedAnvilChunkStorageInvoker)storage;

            invoker.getEntryIterator().forEach(holder ->
            {
                if (holder != null && holder.getWorldChunk() != null && holder.getWorldChunk().getBlockEntityPositions() != null)
                {
                    for (BlockPos pos : holder.getWorldChunk().getBlockEntityPositions())
                    {
                        RenderBox box = new RenderBox(pos);
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box, tileEntityFill);
                        this.renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box, tileEntityOutline);
                    }
                }
            });
        }
    }
}
