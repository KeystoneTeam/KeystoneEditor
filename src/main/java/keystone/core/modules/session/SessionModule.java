package keystone.core.modules.session;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.WorldHistoryChunk;
import keystone.core.utils.FileUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.storage.RegionBasedStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SessionModule implements IKeystoneModule
{
    private MinecraftClient client;
    private LevelSummary level;
    private boolean dirty;
    private boolean revertingSessionChanges;

    @Override public boolean isEnabled() { return true; }
    @Override
    public void postInit()
    {
        client = MinecraftClient.getInstance();
        resetModule();
    }
    @Override
    public void resetModule()
    {
        FileUtils.deleteRecursively(KeystoneDirectories.getSessionDirectory(), true);
    }

    public void registerChange(WorldHistoryChunk chunk)
    {
        this.dirty = true;

        ChunkPos chunkPos = new ChunkPos(chunk.chunkX, chunk.chunkZ);
        String fileName = "r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + RegionBasedStorage.MCA_EXTENSION;
        Path sourcePath = DimensionType.getSaveDirectory(chunk.getRegistryKey(), KeystoneDirectories.getWorldDirectory().toPath()).resolve("region").resolve(fileName);
        Path destPath = DimensionType.getSaveDirectory(chunk.getRegistryKey(), KeystoneDirectories.getSessionDirectory().toPath()).resolve("region").resolve(fileName);

        try
        {
            if (!sourcePath.toFile().exists()) return;
            if (destPath.toFile().exists()) return;
            if (!destPath.getParent().toFile().exists()) destPath.getParent().toFile().mkdirs();
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void markDirty() { dirty = true; }
    public void setLevel(LevelSummary level) { this.level = level; }
    public void revertChanges()
    {
        if (this.revertingSessionChanges) return;
        this.revertingSessionChanges = true;
        Keystone.LOGGER.info("Reverting Session...");

        // Close World
        boolean keystoneEnabled = Keystone.isActive();
        Keystone.disableKeystone();
        client.world.disconnect();
        client.disconnect(new MessageScreen(Text.translatable("keystone.session.revertingChanges")));

        // Revert Changes
        try
        {
            FileUtils.copyRecursively(KeystoneDirectories.getSessionDirectory().toPath(), KeystoneDirectories.getWorldDirectory().toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            Keystone.forEachModule(IKeystoneModule::resetModule);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Re-open world
            client.createIntegratedServerLoader().start(new SelectWorldScreen(new TitleScreen()), level.getName());
            if (keystoneEnabled) Keystone.enableKeystone();
            Keystone.LOGGER.info("Done Reverting Session");

            this.revertingSessionChanges = false;
            this.dirty = false;
        }
    }

    public boolean isDirty() { return this.dirty; }
    public boolean isRevertingSessionChanges() { return this.revertingSessionChanges; }
}
