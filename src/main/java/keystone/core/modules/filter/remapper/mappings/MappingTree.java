package keystone.core.modules.filter.remapper.mappings;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.KeystoneMod;
import keystone.core.modules.filter.remapper.descriptors.ClassDescriptor;
import keystone.core.modules.filter.remapper.descriptors.MethodDescriptorParser;
import keystone.core.modules.filter.remapper.enums.MappingType;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.utils.FileUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MappingTree extends AbstractMappingContainer
{
    public record MethodMappingInfo(Class<?> declaringClass, Class<?>[] parameterTypes, Method method, Mapping mapping) {}
    private final Map<RemappingDirection, Map<String, List<MethodMappingInfo>>> methodMappings = new HashMap<>();
    
    private MappingTree() { }
    
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
            Keystone.LOGGER.info("Done parsing mappings stream.");
        }
        
        // Return loaded mappings
        Keystone.LOGGER.info("Building method mappings...");
        mappings.buildMethodMappings();
        Keystone.LOGGER.info("Done building method mappings.");
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
    private void parseMapping(Scanner scanner)
    {
        List<Mapping> mappings = MappingParser.parse(scanner);
        for (Mapping mapping : mappings) putMapping(mapping);
    }
    private void buildMethodMappings()
    {
        forEachMapping(MappingType.METHOD, mapping ->
        {
            try
            {
                // Build the full descriptor of this method's declaring class
                ClassDescriptor declaringClassDescriptor = ClassDescriptor.fromMapping((Mapping) mapping.getParent());
                Class<?> declaringClass = KeystoneMod.class.getClassLoader().loadClass(declaringClassDescriptor.getClassLoaderName());
                
                // Parse the method descriptor
                Optional<Class<?>[]> parameterTypes = MethodDescriptorParser.parseDescriptor(mapping.getNative(), this);
                if (parameterTypes.isEmpty())
                {
                    Keystone.LOGGER.warn("Could not parse descriptor of method " + mapping.getDeobfuscated() + "!");
                    return;
                }
                
                // Find the reflection method
                Optional<Method> method = findMethod(declaringClass, mapping, parameterTypes.get());
                if (method.isEmpty())
                {
                    Keystone.LOGGER.warn("Method " + mapping.getDeobfuscated() + " is missing a reflection method!");
                    return;
                }
                
                // Build the MethodMappingInfo
                MethodMappingInfo methodInfo = new MethodMappingInfo(declaringClass, parameterTypes.get(), method.get(), mapping);
    
                // Add Obfuscating Info
                Map<String, List<MethodMappingInfo>> map = methodMappings.computeIfAbsent(RemappingDirection.OBFUSCATING, type -> new TreeMap<>());
                List<MethodMappingInfo> infos = map.computeIfAbsent(mapping.getDeobfuscated().substring(0, mapping.getDeobfuscated().indexOf('(')), ignored -> new ArrayList<>());
                infos.add(methodInfo);
    
                // Add Deobfuscating Info
                map = methodMappings.computeIfAbsent(RemappingDirection.DEOBFUSCATING, type -> new TreeMap<>());
                infos = map.computeIfAbsent(mapping.getObfuscated().substring(0, mapping.getObfuscated().indexOf('(')), ignored -> new ArrayList<>());
                infos.add(methodInfo);
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }, true);
    }
    private Optional<Method> findMethod(Class<?> clazz, Mapping mapping, Class<?>... parameterTypes)
    {
        Optional<Method> method = findMethod(clazz, mapping.getObfuscated().substring(0, mapping.getObfuscated().indexOf('(')), parameterTypes);
        if (method.isPresent()) return method;
        else return findMethod(clazz, mapping.getDeobfuscated().substring(0, mapping.getDeobfuscated().indexOf('(')), parameterTypes);
    }
    private Optional<Method> findMethod(Class<?> clazz, String name, Class<?>... parameterTypes)
    {
        try { return Optional.of(clazz.getDeclaredMethod(name, parameterTypes)); }
        catch (Throwable ignored)
        {
            if (clazz.getSuperclass() != null) return findMethod(clazz.getSuperclass(), name, parameterTypes);
            else return Optional.empty();
        }
    }
    //endregion
    
    public List<MethodMappingInfo> getPossibleMethodMappings(RemappingDirection direction, String name)
    {
        return methodMappings.get(direction).get(name);
    }
}
