package keystone.core.modules.filter.remapper.mappings;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.KeystoneMod;
import keystone.core.utils.FileUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MappingTree extends AbstractMappingContainer
{
    //region Built-In
    public static MappingTree builtin()
    {
        long start = System.currentTimeMillis();
        MappingTree tree = resource("filter_mappings.zip");
        long duration = System.currentTimeMillis() - start;
        Keystone.LOGGER.info("Loading Mappings Took " + duration + "ms.");
        return tree;
    }
    //endregion
    //region Yarn
    public static MappingTree yarn()
    {
        // Get Mapping Data from Game Version
        String mappingsVersion = SharedConstants.getGameVersion().getName();
        Path mappingsCache = KeystoneDirectories.getKeystoneSubdirectory(".mappings").resolve("yarn").resolve(mappingsVersion + ".zip");
        
        // Download Mappings if Necessary
        if (!mappingsCache.toFile().exists())
        {
            try
            {
                downloadYarn(mappingsVersion, mappingsCache);
            }
            catch (Exception e)
            {
                reportException("Failed to download Yarn mappings!", e);
                return new MappingTree();
            }
        }
        
        // Parse Mappings
        return loadFromZipFile(mappingsCache.toFile());
    }
    private static void downloadYarn(String mappingsVersion, Path cachePath) throws Exception
    {
        // Get Yarn Mappings URL
        String urlString = "https://github.com/FabricMC/yarn/archive/" + mappingsVersion + ".zip";
        URL url = new URL(urlString);
    
        // Download the Yarn Repository
        String tempName = UUID.randomUUID().toString();
        File tempFile = cachePath.getParent().resolve(tempName + ".zip").toFile();
        try
        {
            FileUtils.DownloadHandle download = FileUtils.download(url, tempFile, null);
            download.complete();
    
            // Extract the Mappings
            Path mappingsFolder = cachePath.getParent().resolve(tempName);
            try
            {
                FileUtils.extract(tempFile, mappingsFolder, "yarn-" + mappingsVersion, "mappings");
                // TODO: Recompress mappings folder
            }
            
            // Cleanup Temporary Mappings Folder
            finally
            {
                if (mappingsFolder.toFile().exists()) FileUtils.deleteRecursively(mappingsFolder.toFile(), false);
            }
        }
        
        // Cleanup Temporary Repository Zip
        finally
        {
            if (tempFile.exists()) FileUtils.deleteRecursively(tempFile, false);
        }
    }
    //endregion
    // region Zip Mappings
    public static MappingTree resource(String name)
    {
        try (InputStream mappingsStream = KeystoneMod.class.getResourceAsStream("/" + name);
             ZipInputStream zipStream = new ZipInputStream(Objects.requireNonNull(mappingsStream)))
        {
            return loadFromZipStream(zipStream);
        }
        catch (Exception e)
        {
            reportException("Failed to create built-in mappings resource stream!", e);
            return new MappingTree();
        }
    }
    public static MappingTree loadFromZipFile(File zipFile)
    {
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zipStream = new ZipInputStream(fis))
        {
            return loadFromZipStream(zipStream);
        }
        catch (IOException e)
        {
            reportException("Failed to open zip file '" + zipFile.getName() + "'!", e);
            return new MappingTree();
        }
    }
    public static MappingTree loadFromZipStream(ZipInputStream zipStream)
    {
        MappingTree mappings = new MappingTree();
        
        // Parse Mappings Archive
        Keystone.LOGGER.info("Parsing mappings stream...");
        ZipEntry entry = null;
        try
        {
            // For each entry in the mappings zip
            while ((entry = zipStream.getNextEntry()) != null)
            {
                // Process Mapping File
                if (entry.getName().endsWith(".mapping"))
                {
                    Scanner scanner = new Scanner(zipStream);
                    mappings.parseMapping(scanner);
                }
            }
        }
        catch (IOException e)
        {
            if (entry != null) reportException("Failed to process mappings stream! Failed entry name: " + entry.getName(), e);
            else reportException("Failed to process mappings stream!", e);
        }
        finally
        {
            Keystone.LOGGER.info("Done parsing mappings stream");
        }
        
        // Return loaded mappings
        return mappings;
    }
    //endregion
    //region Private Helpers
    private static void reportException(String errorMessage, Exception e)
    {
        if (e != null) errorMessage += " " + e.getMessage();
        
        Keystone.LOGGER.error(errorMessage);
        if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().player.sendMessage(Text.literal(errorMessage).styled(style -> style.withColor(Formatting.RED)));
        
        if (e != null) e.printStackTrace();
    }
    //endregion
    
    //region Parsers
    private void parseMapping(Scanner scanner)
    {
        List<Mapping> mappings = MappingParser.parse(scanner);
        for (Mapping mapping : mappings) putMapping(mapping);
    }
    //endregion
}
