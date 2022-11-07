package keystone.core.modules.session;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.gui.screens.PromptQuestionScreen;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
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
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

public class SessionModule implements IKeystoneModule
{
    private MinecraftClient client;
    private LevelSummary level;
    private HistoryModule historyModule;
    private boolean revertingSessionChanges;

    @Override public boolean isEnabled() { return true; }
    @Override
    public void postInit()
    {
        client = MinecraftClient.getInstance();
        historyModule = Keystone.getModule(HistoryModule.class);

        InputEvents.KEY_PRESSED.register(this::onKeyPressed);

        resetModule();
    }
    @Override
    public void resetModule()
    {
        this.revertingSessionChanges = false;
    }

    private void onKeyPressed(int key, int action, int scancode, int modifiers)
    {
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_S && modifiers == GLFW.GLFW_MOD_CONTROL)
        {
            commitChanges();
        }
    }
    
    public void setLevel(LevelSummary level) { this.level = level; }
    public boolean isRevertingSessionChanges() { return this.revertingSessionChanges; }
    
    public void registerChange(WorldHistoryChunk chunk)
    {
        ChunkPos chunkPos = new ChunkPos(chunk.chunkX, chunk.chunkZ);
        String fileName = "r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + RegionBasedStorage.MCA_EXTENSION;
        Path sourcePath = DimensionType.getSaveDirectory(chunk.getRegistryKey(), KeystoneDirectories.getWorldDirectory().toPath()).resolve("region").resolve(fileName);
        Path sessionDestPath = DimensionType.getSaveDirectory(chunk.getRegistryKey(), KeystoneDirectories.getWorldSessionDirectory().toPath()).resolve("region").resolve(fileName);
        Path backupDestPath = DimensionType.getSaveDirectory(chunk.getRegistryKey(), KeystoneDirectories.getWorldBackupDirectory().toPath()).resolve("region").resolve(fileName);
        writeSessionPropertiesFile();
        
        try
        {
            if (!sourcePath.toFile().exists()) return;
            
            if (!sessionDestPath.toFile().exists())
            {
                if (!sessionDestPath.getParent().toFile().exists()) sessionDestPath.getParent().toFile().mkdirs();
                Files.copy(sourcePath, sessionDestPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
            
            if (!backupDestPath.toFile().exists())
            {
                if (!backupDestPath.getParent().toFile().exists()) backupDestPath.getParent().toFile().mkdirs();
                Files.copy(sourcePath, backupDestPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void commitChanges()
    {
        resetModule();
        FileUtils.deleteRecursively(KeystoneDirectories.getWorldSessionDirectory(), true);
        KeystoneLifecycleEvents.COMMIT_SESSION.invoker().commitSession();
    }
    public void revertChanges(boolean closeWorld)
    {
        if (this.revertingSessionChanges) return;
        this.revertingSessionChanges = true;
        Keystone.LOGGER.info("Reverting Session...");

        // Close World
        boolean keystoneEnabled = Keystone.isEnabled();
        Keystone.disableKeystone();
        client.world.disconnect();
        client.disconnect(new MessageScreen(Text.translatable("keystone.session.revertingChanges")));

        // Revert Changes
        try
        {
            FileUtils.copyRecursively(KeystoneDirectories.getWorldSessionDirectory().toPath(), KeystoneDirectories.getWorldDirectory().toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            FileUtils.deleteRecursively(KeystoneDirectories.getWorldSessionDirectory(), true);
            KeystoneLifecycleEvents.REVERT_SESSION.invoker().revertSession();
            Keystone.forEachModule(IKeystoneModule::resetModule);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Re-open world
            this.revertingSessionChanges = true;
            if (!closeWorld)
            {
                client.createIntegratedServerLoader().start(new SelectWorldScreen(new TitleScreen()), level.getName());
                if (keystoneEnabled) Keystone.enableKeystone();
            }
            Keystone.LOGGER.info("Done Reverting Session");

            this.revertingSessionChanges = false;
            historyModule.commitOrRevertSession();
        }
    }
    public void repairSession()
    {
        Optional<Properties> properties = readSessionPropertiesFile();
        properties.ifPresent(value -> KeystoneLifecycleEvents.REPAIR_SESSION.invoker().repairSession(value));
    }
    private Optional<Properties> readSessionPropertiesFile()
    {
        File sessionDirectory = KeystoneDirectories.getWorldSessionDirectory();
        if (!sessionDirectory.exists()) return Optional.empty();
    
        Properties properties = new Properties();
        try
        {
            FileInputStream outputStream = new FileInputStream(KeystoneDirectories.getWorldSessionDirectory().toPath().resolve("session.info").toFile());
            properties.load(outputStream);
            outputStream.close();
            return Optional.of(properties);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    private void writeSessionPropertiesFile()
    {
        Properties properties = new Properties();
        KeystoneLifecycleEvents.SAVE_SESSION_INFO.invoker().saveSessionInfo(properties);
    
        try
        {
            FileOutputStream outputStream = new FileOutputStream(KeystoneDirectories.getWorldSessionDirectory().toPath().resolve("session.info").toFile());
            properties.store(outputStream, "");
            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void promptUncommittedChanges() { promptUncommittedChanges(this::commitChanges, () -> revertChanges(false), null); }
    public void promptUncommittedChanges(Runnable committed, Runnable reverted, Runnable cancelled)
    {
        if (historyModule.getUnsavedChanges() == 0)
        {
            if (committed != null) committed.run();
        }
        else MinecraftClient.getInstance().setScreenAndRender(new PromptQuestionScreen(null, Text.translatable("keystone.session.promptUncommitted"),
                Text.translatable("keystone.session.commitButton"), () -> { if (committed != null) committed.run(); })
                .addDenyButton(Text.translatable("keystone.session.revertButton"), () -> { if (reverted != null) reverted.run(); })
                .addCancelButton(Text.translatable("keystone.cancel"), () -> { if (cancelled != null) cancelled.run(); })
        );
    }
}
