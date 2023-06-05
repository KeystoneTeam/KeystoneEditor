package keystone.core.modules.filter.remapper;

import keystone.api.Keystone;
import keystone.api.KeystoneCache;
import keystone.core.KeystoneMod;
import keystone.core.modules.filter.MultiClassLoader;
import keystone.core.utils.FileUtils;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.mappings.TinyRemapperMappingsHelper;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.tinyremapper.*;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

public class FilterRemapper
{
    private static final FabricLauncher LAUNCHER;
    public static final IMappingProvider TARGET_TO_NAMED;
    public static final IMappingProvider NAMED_TO_TARGET;
    public static final Path MINECRAFT_JAR;
    public static final Path NAMED_MINECRAFT_JAR;
    public static final MultiClassLoader REMAPPED_CLASS_LOADER;
    
    static
    {
        LAUNCHER = FabricLauncherBase.getLauncher();
    
        try
        {
            File minecraftJarFile = new File(MinecraftClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            MINECRAFT_JAR = minecraftJarFile.toPath();
            NAMED_MINECRAFT_JAR = KeystoneCache.getCacheDirectory().resolve(SharedConstants.getGameVersion().getName() + "-named.jar");

            ClassLoader keystoneLoader = KeystoneMod.class.getClassLoader();
            ClassLoader fabricLoader = FabricLauncher.class.getClassLoader();
            ClassLoader minecraftLoader = MinecraftClient.class.getClassLoader();
            ClassLoader platformLoader = ClassLoader.getPlatformClassLoader();
            ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
            REMAPPED_CLASS_LOADER = MultiClassLoader.from(keystoneLoader, fabricLoader, minecraftLoader, platformLoader, systemLoader);
            
            try (InputStream resourceStream = KeystoneMod.class.getResourceAsStream("/filter_mappings.tiny");
                 InputStreamReader resourceStreamReader = new InputStreamReader(resourceStream);
                 BufferedReader mappingsReader = new BufferedReader(resourceStreamReader))
            {
                TinyTree mappings = TinyMappingFactory.load(mappingsReader, true);
                TARGET_TO_NAMED = TinyRemapperMappingsHelper.create(mappings, LAUNCHER.getMappingConfiguration().getTargetNamespace(), "named");
                NAMED_TO_TARGET = TinyRemapperMappingsHelper.create(mappings, "named", LAUNCHER.getMappingConfiguration().getTargetNamespace());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static void remapFile(Path input, Path output, IMappingProvider mapping) throws IOException
    {
        remapFiles(List.of(input), List.of(output), mapping);
    }
    public static void remapFiles(List<Path> inputFiles, List<Path> outputFiles, IMappingProvider mapping) throws IOException
    {
        Path tempDirectory = KeystoneCache.newTempDirectory();
        
        // Print Remapping Start
        long startTime = System.currentTimeMillis();
        Keystone.LOGGER.info("Beginning remapping operation. Files:");
        for (Path inputFile : inputFiles) Keystone.LOGGER.info("  " + inputFile);
        
        // Create remapper
        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(mapping)
                .renameInvalidLocals(false)
                .build();
        
        // Load classpath into remapper
        Set<Path> depPaths = new HashSet<>();
        for (Path path : LAUNCHER.getClassPath())
        {
            if (!inputFiles.contains(path))
            {
                Keystone.LOGGER.debug("Appending '" + path + "' to FilterRemapper classpath");
                depPaths.add(path);
                remapper.readClassPathAsync(path);
            }
        }
    
        // Create remapping lists
        List<OutputConsumerPath> outputConsumers = new ArrayList<>(inputFiles.size());
        List<InputTag> inputTags = new ArrayList<>(inputFiles.size());
        List<Path> tmpFiles = new ArrayList<>();
        
        // Perform Remapping
        try
        {
            // For each input file
            for (Path inputFile : inputFiles)
            {
                // Get source and temp file paths
                Path tmpFile = tempDirectory.resolve(inputFile.toFile().getName() + ".tmp");
                tmpFiles.add(tmpFile);
                depPaths.add(tmpFile);
        
                // Create input tag and output consumer
                InputTag inputTag = remapper.createInputTag();
                OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(tmpFile)
                        // force jar despite the .tmp extension
                        .assumeArchive(true)
                        .build();
        
                outputConsumers.add(outputConsumer);
                inputTags.add(inputTag);
        
                outputConsumer.addNonClassFiles(inputFile, NonClassCopyMode.FIX_META_INF, remapper);
                remapper.readInputsAsync(inputTag, inputFile);
            }
    
            for (int i = 0; i < inputFiles.size(); i++) remapper.apply(outputConsumers.get(i), inputTags.get(i));
        }
        finally
        {
            // Cleanup
            for (OutputConsumerPath outputConsumer : outputConsumers) outputConsumer.close();
            remapper.finish();
        }
        
        // Cleanup temp files
        for (Path p : depPaths)
        {
            try { p.getFileSystem().close(); }
            catch (Exception ignored) { }
            
            try { FileSystems.getFileSystem(new URI("jar:" + p.toUri())).close(); }
            catch (Exception ignored) { }
        }
    
        List<Path> missing = new ArrayList<>();
    
        // For each remap operation
        for (int i = 0; i < inputFiles.size(); i++)
        {
            // Get input, temp, and output paths
            Path inputFile = inputFiles.get(i);
            Path tmpFile = tmpFiles.get(i);
            Path outputFile = outputFiles.get(i);
        
            // Check if the temp file contains at least one class file
            boolean found;
            try (JarFile jar = new JarFile(tmpFile.toFile())) { found = jar.stream().anyMatch((e) -> e.getName().endsWith(".class")); }
        
            // If the temp file does not contain any classes
            if (!found)
            {
                // Mark it as missing and delete it
                missing.add(inputFile);
                Files.delete(tmpFile);
            }
            
            // If the temp file contains classes, rename it to the output file path
            else Files.move(tmpFile, outputFile);
        }
    
        // Throw exception if a remap operation failed
        if (!missing.isEmpty()) throw new RuntimeException("Generated deobfuscated JARs contain no classes: " + missing);
        
        // Print Timing
        long duration = System.currentTimeMillis() - startTime;
        Keystone.LOGGER.info("Finished remapping operation in " + duration + "ms.");
        FileUtils.deleteRecursively(tempDirectory.toFile(), false);
    }
    
    public static void init() throws IOException
    {
        if (NAMED_MINECRAFT_JAR.toFile().exists()) Keystone.LOGGER.info("A remapped Minecraft jar is already cached. Skipping Minecraft remapping.");
        else remapFile(MINECRAFT_JAR, NAMED_MINECRAFT_JAR, TARGET_TO_NAMED);
    
        URLClassLoader namedJarLoader = new URLClassLoader(new URL[] { NAMED_MINECRAFT_JAR.toUri().toURL() });
        REMAPPED_CLASS_LOADER.prependLoader(namedJarLoader);
    }
}
